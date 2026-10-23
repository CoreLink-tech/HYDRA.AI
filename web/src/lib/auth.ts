import { and, eq, gt, isNull } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { hashToken, newBearerToken, randomId } from "./crypto";

export const SESSION_TTL_MS = 90 * 24 * 60 * 60 * 1000;

export type AuthedUser = {
  id: string;
  email: string | null;
  role: "user" | "admin";
  status: "active" | "suspended";
};

/** Creates a session row and returns the raw bearer token (shown to the client exactly once). */
export async function issueSession(userId: string, userAgent: string | null): Promise<{
  token: string;
  expiresAt: number;
}> {
  const db = getDb();
  const token = newBearerToken();
  const expiresAt = Date.now() + SESSION_TTL_MS;
  await db.insert(schema.sessions).values({
    id: randomId(12),
    userId,
    tokenHash: hashToken(token),
    expiresAt,
    userAgent: userAgent?.slice(0, 200) ?? null,
  });
  return { token, expiresAt };
}

/** Resolves `Authorization: Bearer <token>` to an active user, or null. */
export async function getAuthedUser(req: Request): Promise<AuthedUser | null> {
  const header = req.headers.get("authorization") ?? "";
  const match = /^Bearer\s+(hy_[A-Za-z0-9_-]{20,})$/.exec(header);
  if (!match?.[1]) return null;

  const db = getDb();
  const rows = await db
    .select({
      id: schema.users.id,
      email: schema.users.email,
      role: schema.users.role,
      status: schema.users.status,
    })
    .from(schema.sessions)
    .innerJoin(schema.users, eq(schema.sessions.userId, schema.users.id))
    .where(
      and(
        eq(schema.sessions.tokenHash, hashToken(match[1])),
        isNull(schema.sessions.revokedAt),
        gt(schema.sessions.expiresAt, Date.now()),
      ),
    )
    .limit(1);

  const user = rows[0];
  if (!user || user.status !== "active") return null;
  return user;
}

export async function revokeSession(req: Request): Promise<void> {
  const header = req.headers.get("authorization") ?? "";
  const match = /^Bearer\s+(hy_[A-Za-z0-9_-]{20,})$/.exec(header);
  if (!match?.[1]) return;
  await getDb()
    .update(schema.sessions)
    .set({ revokedAt: Date.now() })
    .where(eq(schema.sessions.tokenHash, hashToken(match[1])));
}
