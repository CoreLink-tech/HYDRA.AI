import { and, count, desc, eq, isNull, gt, or, isNotNull } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { getAuthedUser } from "@/lib/auth";
import { randomId, sha256Hex } from "@/lib/crypto";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";
import { getEntitlement, SHARE_LIMITS } from "@/lib/entitlements";
import {
  decodedLength,
  MAX_CIPHERTEXT_BYTES,
  MAX_MESSAGES_PER_SHARE,
  MAX_SHARE_TTL_HOURS,
  SHARE_MODES,
  type ShareMode,
} from "@/lib/shares";

/**
 * POST /api/v1/shares (Bearer)
 * Uploads an end-to-end encrypted conversation. The app encrypts with AES-GCM using a key that
 * never leaves the device; the key is placed in the share link fragment (#k=...) by the app.
 * The server only stores { ciphertext, iv, sha256 } and cannot decrypt it.
 *
 * body: { ciphertext: base64, iv: base64, mode, messageCount, expiresInHours? }
 * returns: { id, createdAt, expiresAt }. The app builds the link as https://<site>/s/<id>#k=<key>.
 */
export async function POST(req: Request) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");

  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");

  const ciphertext = str(body, "ciphertext", { min: 16, max: Math.ceil(MAX_CIPHERTEXT_BYTES * 4 / 3) + 8 });
  const iv = str(body, "iv", { min: 12, max: 32 });
  const modeRaw = body.mode;
  const messageCount = body.messageCount;
  const ttlRaw = body.expiresInHours;

  if (!ciphertext || !iv) return apiError("bad_request", "ciphertext and iv (base64) are required.");
  const byteSize = decodedLength(ciphertext);
  if (byteSize === null) return apiError("bad_request", "ciphertext must be base64.");
  if (byteSize > MAX_CIPHERTEXT_BYTES) {
    return apiError("payload_too_large", "Shared conversation is larger than 2 MB.");
  }
  if (decodedLength(iv) === null) return apiError("bad_request", "iv must be base64.");
  if (typeof modeRaw !== "string" || !(SHARE_MODES as readonly string[]).includes(modeRaw)) {
    return apiError("bad_request", "mode must be one of " + SHARE_MODES.join(", ") + ".");
  }
  const mode = modeRaw as ShareMode;
  if (typeof messageCount !== "number" || !Number.isInteger(messageCount) || messageCount < 0 || messageCount > MAX_MESSAGES_PER_SHARE) {
    return apiError("bad_request", "messageCount must be an integer from 0 to " + MAX_MESSAGES_PER_SHARE + ".");
  }
  let ttlHours: number | null = null;
  if (ttlRaw !== undefined && ttlRaw !== null) {
    if (typeof ttlRaw !== "number" || !Number.isFinite(ttlRaw) || ttlRaw <= 0 || ttlRaw > MAX_SHARE_TTL_HOURS) {
      return apiError("bad_request", "expiresInHours must be between 1 and " + MAX_SHARE_TTL_HOURS + ".");
    }
    ttlHours = ttlRaw;
  }

  const db = getDb();
  const entitlement = await getEntitlement(user.id);
  const [live] = await db
    .select({ n: count() })
    .from(schema.shareRecords)
    .where(and(eq(schema.shareRecords.ownerUserId, user.id), isNull(schema.shareRecords.revokedAt)));
  if ((live?.n ?? 0) >= SHARE_LIMITS[entitlement.plan]) {
    return apiError("quota_exceeded", "Live share limit reached. Revoke an old share or upgrade to Pro.", {
      limit: SHARE_LIMITS[entitlement.plan],
    });
  }

  const now = Date.now();
  const blobId = randomId(16);
  const shareId = randomId(16);
  const expiresAt = ttlHours ? now + ttlHours * 3600_000 : null;

  await db.insert(schema.shareBlobs).values({
    id: blobId,
    ciphertext,
    iv,
    byteSize,
    sha256: sha256Hex(Buffer.from(ciphertext, "base64")),
    createdAt: now,
  });
  await db.insert(schema.shareRecords).values({
    id: shareId,
    ownerUserId: user.id,
    blobId,
    mode,
    messageCount,
    createdAt: now,
    expiresAt,
  });

  return apiOk({ id: shareId, createdAt: now, expiresAt }, { status: 201 });
}

/** GET /api/v1/shares (Bearer): the caller's own share records (metadata only, no ciphertext). */
export async function GET(req: Request) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");

  const rows = await getDb()
    .select({
      id: schema.shareRecords.id,
      mode: schema.shareRecords.mode,
      messageCount: schema.shareRecords.messageCount,
      createdAt: schema.shareRecords.createdAt,
      expiresAt: schema.shareRecords.expiresAt,
      revokedAt: schema.shareRecords.revokedAt,
      viewCount: schema.shareRecords.viewCount,
      takedownAt: schema.shareRecords.takedownAt,
    })
    .from(schema.shareRecords)
    .where(eq(schema.shareRecords.ownerUserId, user.id))
    .orderBy(desc(schema.shareRecords.createdAt))
    .limit(200);

  return apiOk({
    shares: rows.map((r) => ({
      ...r,
      active: r.revokedAt === null && r.takedownAt === null && (r.expiresAt === null || r.expiresAt > Date.now()),
    })),
  });
}
