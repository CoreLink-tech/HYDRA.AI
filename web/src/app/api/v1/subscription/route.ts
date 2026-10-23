import { getAuthedUser } from "@/lib/auth";
import { apiError, apiOk } from "@/lib/http";
import { getEntitlement, SHARE_LIMITS } from "@/lib/entitlements";

/**
 * GET /api/v1/subscription (Bearer)
 * Returns the entitlement the app should use to unlock Pro features. The app caches it locally
 * so Pro features keep working offline until expiresAt.
 */
export async function GET(req: Request) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");

  const e = await getEntitlement(user.id);
  return apiOk({
    plan: e.plan,
    status: e.status,
    productId: e.productId,
    expiresAt: e.expiresAt,
    autoRenewing: e.autoRenewing,
    limits: { liveShares: SHARE_LIMITS[e.plan] },
    // Core offline chat is never gated by this response. Downloaded models keep working after expiry.
    coreOfflineAccess: true,
  });
}
