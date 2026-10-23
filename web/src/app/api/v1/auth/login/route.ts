import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { issueSession } from "@/lib/auth";
import { verifyPassword } from "@/lib/crypto";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";

/** POST /api/v1/auth/login  { email, password } -> { token, expiresAt } */
export async function POST(req: Request) {
  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");

  const email = str(body, "email", { min: 5, max: 254 });
  const password = str(body, "password", { min: 1, max: 200 });
  if (!email || !password) return apiError("bad_request", "email and password are required.");

  const rows = await getDb()
    .select()
    .from(schema.users)
    .where(eq(schema.users.email, email.toLowerCase()))
    .limit(1);
  const user = rows[0];

  // Same error for unknown email and wrong password, to avoid account enumeration.
  const ok = user?.passwordHash ? await verifyPassword(password, user.passwordHash) : false;
  if (!user || !ok) return apiError("unauthorized", "Email or password is incorrect.");
  if (user.status !== "active") return apiError("forbidden", "This account is suspended.");

  await getDb().update(schema.users).set({ lastSeenAt: Date.now() }).where(eq(schema.users.id, user.id));
  const session = await issueSession(user.id, req.headers.get("user-agent"));
  return apiOk({ userId: user.id, token: session.token, expiresAt: session.expiresAt });
}
