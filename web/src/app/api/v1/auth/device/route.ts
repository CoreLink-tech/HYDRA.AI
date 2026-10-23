import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { issueSession } from "@/lib/auth";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";
import { randomId } from "@/lib/crypto";

/**
 * POST /api/v1/auth/device
 * Creates an anonymous account bound to one app install. The Android app generates a stable
 * random deviceId (UUIDv4) and keeps it in its own storage. The server never sees chat data.
 *
 * TODO(abuse): add rate limiting per IP (Cloudflare rate-limit rule or Upstash) before launch.
 */
export async function POST(req: Request) {
  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");

  const deviceId = str(body, "deviceId", { min: 16, max: 128 });
  if (!deviceId) return apiError("bad_request", "deviceId must be 16 to 128 characters.");

  const db = getDb();
  const existing = await db
    .select({ id: schema.users.id })
    .from(schema.users)
    .where(eq(schema.users.deviceId, deviceId))
    .limit(1);
  if (existing[0]) {
    // An install that already registered must not get a second account or a token for the first one.
    return apiError("conflict", "This device is already registered. Sign in with your account instead.");
  }

  const userId = randomId(12);
  await db.insert(schema.users).values({ id: userId, deviceId, lastSeenAt: Date.now() });
  const session = await issueSession(userId, req.headers.get("user-agent"));
  return apiOk({ userId, token: session.token, expiresAt: session.expiresAt }, { status: 201 });
}
