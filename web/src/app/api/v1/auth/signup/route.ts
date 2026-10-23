import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { issueSession } from "@/lib/auth";
import { hashPassword, randomId } from "@/lib/crypto";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";

/**
 * POST /api/v1/auth/signup  { email, password, displayName? }
 * Email account for syncing subscription status across installs. No chat content is stored.
 *
 * TODO(email): send a verification email (Resend / SES) before enabling sharing for unverified accounts.
 * TODO(abuse): rate limit by IP and email.
 */
export async function POST(req: Request) {
  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");

  const email = str(body, "email", { min: 5, max: 254 });
  const password = str(body, "password", { min: 10, max: 200 });
  const displayName = str(body, "displayName", { max: 80, optional: true });
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return apiError("bad_request", "A valid email is required.");
  }
  if (!password) return apiError("bad_request", "Password must be at least 10 characters.");

  const normalised = email.toLowerCase();
  const db = getDb();
  const taken = await db
    .select({ id: schema.users.id })
    .from(schema.users)
    .where(eq(schema.users.email, normalised))
    .limit(1);
  if (taken[0]) return apiError("conflict", "An account with this email already exists.");

  const userId = randomId(12);
  await db.insert(schema.users).values({
    id: userId,
    email: normalised,
    displayName: displayName ?? null,
    passwordHash: await hashPassword(password),
    lastSeenAt: Date.now(),
  });
  const session = await issueSession(userId, req.headers.get("user-agent"));
  return apiOk({ userId, token: session.token, expiresAt: session.expiresAt }, { status: 201 });
}
