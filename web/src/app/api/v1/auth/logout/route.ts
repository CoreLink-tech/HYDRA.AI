import { revokeSession } from "@/lib/auth";
import { apiOk } from "@/lib/http";

/** POST /api/v1/auth/logout (Bearer) revokes the current session token. */
export async function POST(req: Request) {
  await revokeSession(req);
  return apiOk({ ok: true });
}
