import { and, eq, isNotNull, isNull, or, gt, sql } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { getAuthedUser } from "@/lib/auth";
import { apiError, apiOk } from "@/lib/http";

type Ctx = { params: Promise<{ id: string }> };

const ID_RE = /^[A-Za-z0-9_-]{16,64}$/;

/**
 * GET /api/v1/shares/:id
 * Public retrieval for recipients (no account needed). The id is an unguessable 128-bit capability.
 * Returns ciphertext + iv only. The decryption key is in the link fragment and is never sent here.
 * Returns 410 for revoked, expired or taken-down shares.
 *
 * TODO(abuse): rate limit retrievals per IP to slow id enumeration.
 */
export async function GET(_req: Request, ctx: Ctx) {
  const { id } = await ctx.params;
  if (!ID_RE.test(id)) return apiError("not_found", "Share not found.");

  const db = getDb();
  const rows = await db
    .select({
      id: schema.shareRecords.id,
      mode: schema.shareRecords.mode,
      messageCount: schema.shareRecords.messageCount,
      createdAt: schema.shareRecords.createdAt,
      expiresAt: schema.shareRecords.expiresAt,
      revokedAt: schema.shareRecords.revokedAt,
      takedownAt: schema.shareRecords.takedownAt,
      ciphertext: schema.shareBlobs.ciphertext,
      iv: schema.shareBlobs.iv,
      sha256: schema.shareBlobs.sha256,
    })
    .from(schema.shareRecords)
    .innerJoin(schema.shareBlobs, eq(schema.shareRecords.blobId, schema.shareBlobs.id))
    .where(eq(schema.shareRecords.id, id))
    .limit(1);

  const share = rows[0];
  if (!share) return apiError("not_found", "Share not found.");
  if (share.revokedAt || share.takedownAt || (share.expiresAt && share.expiresAt <= Date.now())) {
    return apiError("gone", "This shared conversation is no longer available.");
  }

  await db
    .update(schema.shareRecords)
    .set({ viewCount: sql`${schema.shareRecords.viewCount} + 1` })
    .where(eq(schema.shareRecords.id, id));

  return apiOk({
    id: share.id,
    mode: share.mode,
    messageCount: share.messageCount,
    createdAt: share.createdAt,
    expiresAt: share.expiresAt,
    ciphertext: share.ciphertext,
    iv: share.iv,
    sha256: share.sha256,
  });
}

/** DELETE /api/v1/shares/:id (Bearer, owner only): revoke. The blob is deleted immediately. */
export async function DELETE(req: Request, ctx: Ctx) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");

  const { id } = await ctx.params;
  if (!ID_RE.test(id)) return apiError("not_found", "Share not found.");

  const db = getDb();
  const rows = await db
    .select({ blobId: schema.shareRecords.blobId, owner: schema.shareRecords.ownerUserId })
    .from(schema.shareRecords)
    .where(eq(schema.shareRecords.id, id))
    .limit(1);
  const share = rows[0];
  if (!share || share.owner !== user.id) return apiError("not_found", "Share not found.");

  await db
    .update(schema.shareRecords)
    .set({ revokedAt: Date.now() })
    .where(eq(schema.shareRecords.id, id));
  // Ciphertext is removed now, not on a schedule, so revoke really means revoke.
  await db.delete(schema.shareBlobs).where(eq(schema.shareBlobs.id, share.blobId));

  return apiOk({ id, revoked: true });
}
