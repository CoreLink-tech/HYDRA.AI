import { getAuthedUser } from "@/lib/auth";
import { apiError, apiOk } from "@/lib/http";
import { getEntitlement } from "@/lib/entitlements";

/** GET /api/v1/me (Bearer): account summary plus current plan. */
export async function GET(req: Request) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");
  const entitlement = await getEntitlement(user.id);
  return apiOk({
    userId: user.id,
    email: user.email,
    role: user.role,
    plan: entitlement.plan,
  });
}
