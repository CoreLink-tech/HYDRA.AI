import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { getAuthedUser } from "@/lib/auth";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";
import { getEntitlement } from "@/lib/entitlements";
import {
  acknowledgeSubscription,
  fetchSubscription,
  mapPlayState,
  PlayConfigError,
  PlayUpstreamError,
} from "@/lib/google-play";
import { randomId } from "@/lib/crypto";

/** Product ids configured in Play Console. TODO(play): align with the real ids once created. */
const ALLOWED_PRODUCTS = new Set(["hydra_pro_monthly", "hydra_pro_annual"]);

/**
 * POST /api/v1/billing/google-play/verify (Bearer)
 * body: { productId, purchaseToken }
 *
 * The client only tells us which token it bought. The subscription state, expiry and
 * acknowledgement come from Google, never from the request body.
 */
export async function POST(req: Request) {
  const user = await getAuthedUser(req);
  if (!user) return apiError("unauthorized", "Missing or invalid token.");

  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");
  const productId = str(body, "productId", { min: 3, max: 100 });
  const purchaseToken = str(body, "purchaseToken", { min: 20, max: 4096 });
  if (!productId || !purchaseToken) {
    return apiError("bad_request", "productId and purchaseToken are required.");
  }
  if (!ALLOWED_PRODUCTS.has(productId)) return apiError("bad_request", "Unknown productId.");

  let play;
  try {
    play = await fetchSubscription(purchaseToken);
  } catch (err) {
    if (err instanceof PlayConfigError) {
      // TODO(credentials): fills in once the Play service account is configured.
      return apiError("not_configured", "Google Play verification is not configured yet.");
    }
    if (err instanceof PlayUpstreamError) {
      return apiError("upstream_error", "Google Play could not confirm this purchase. Try again shortly.");
    }
    throw err;
  }

  const line = play.lineItems?.find((l) => l.productId === productId) ?? play.lineItems?.[0];
  const db = getDb();

  // A purchase token belongs to one account. Refuse to move it to a different user.
  const owners = await db
    .select({ userId: schema.subscriptions.userId })
    .from(schema.subscriptions)
    .where(eq(schema.subscriptions.purchaseToken, purchaseToken))
    .limit(1);
  if (owners[0] && owners[0].userId !== user.id) {
    return apiError("conflict", "This purchase is already linked to another account.");
  }

  const status = mapPlayState(play.subscriptionState);
  const acknowledged = play.acknowledgementState === "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED";
  const expiresAt = line?.expiryTime ? Date.parse(line.expiryTime) : null;
  const values = {
    userId: user.id,
    plan: "pro" as const,
    productId,
    purchaseToken,
    orderId: play.latestOrderId ?? null,
    status: status as "active" | "grace" | "on_hold" | "paused" | "canceled" | "expired" | "pending",
    autoRenewing: Boolean(line?.autoRenewingPlan?.autoRenewEnabled),
    startsAt: play.startTime ? Date.parse(play.startTime) : null,
    expiresAt,
    acknowledged,
    lastVerifiedAt: Date.now(),
    updatedAt: Date.now(),
  };

  if (owners[0]) {
    await db.update(schema.subscriptions).set(values).where(eq(schema.subscriptions.purchaseToken, purchaseToken));
  } else {
    await db.insert(schema.subscriptions).values({ id: randomId(12), ...values });
  }

  // Acknowledge only after the record is stored, so a crash never leaves a paid-but-unrecorded purchase.
  if (!acknowledged && (status === "active" || status === "grace")) {
    try {
      await acknowledgeSubscription(productId, purchaseToken);
      await db
        .update(schema.subscriptions)
        .set({ acknowledged: true })
        .where(eq(schema.subscriptions.purchaseToken, purchaseToken));
    } catch {
      // TODO(play): retry acknowledgement from a background job; Google refunds unacknowledged purchases after ~3 days.
    }
  }

  const entitlement = await getEntitlement(user.id);
  return apiOk({ plan: entitlement.plan, status, expiresAt, productId });
}
