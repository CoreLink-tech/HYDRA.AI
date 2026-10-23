import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { apiError, apiOk } from "@/lib/http";
import { fetchSubscription, mapPlayState, PlayConfigError, PlayUpstreamError } from "@/lib/google-play";
import { safeEqual } from "@/lib/crypto";

/**
 * POST /api/v1/billing/google-play/rtdn
 * Real-time developer notifications from Google Play (Pub/Sub push). Keeps subscription state
 * (renewal, cancel, expiry, on-hold) in sync without the app being open.
 *
 * Pub/Sub body: { message: { data: base64(JSON), messageId }, subscription }.
 * JSON: { subscriptionNotification: { purchaseToken, subscriptionId, notificationType } }.
 *
 * TODO(play): verify the Pub/Sub push OIDC JWT (audience = this URL). The shared-secret check below
 * is a stop-gap only.
 * TODO(play): create the Pub/Sub topic and point the Play Console RTDN to it.
 */
export async function POST(req: Request) {
  const secret = process.env.PLAY_RTDN_SECRET ?? "";
  const provided = new URL(req.url).searchParams.get("key") ?? "";
  if (!secret || !safeEqual(provided, secret)) return apiError("forbidden", "Forbidden.");

  let body: { message?: { data?: string } };
  try {
    body = (await req.json()) as { message?: { data?: string } };
  } catch {
    return apiError("bad_request", "Invalid JSON.");
  }
  const raw = body.message?.data;
  if (!raw) return apiOk({ ok: true, ignored: true });

  let note: { subscriptionNotification?: { purchaseToken?: string } };
  try {
    note = JSON.parse(Buffer.from(raw, "base64").toString("utf8"));
  } catch {
    return apiError("bad_request", "Undecodable notification.");
  }
  const purchaseToken = note.subscriptionNotification?.purchaseToken;
  if (!purchaseToken) return apiOk({ ok: true, ignored: true });

  const rows = await getDb()
    .select({ productId: schema.subscriptions.productId })
    .from(schema.subscriptions)
    .where(eq(schema.subscriptions.purchaseToken, purchaseToken))
    .limit(1);
  if (!rows[0]) return apiOk({ ok: true, ignored: true });

  try {
    const play = await fetchSubscription(purchaseToken);
    const line = play.lineItems?.[0];
    await getDb()
      .update(schema.subscriptions)
      .set({
        status: mapPlayState(play.subscriptionState) as "active",
        autoRenewing: Boolean(line?.autoRenewingPlan?.autoRenewEnabled),
        expiresAt: line?.expiryTime ? Date.parse(line.expiryTime) : null,
        lastVerifiedAt: Date.now(),
        updatedAt: Date.now(),
      })
      .where(eq(schema.subscriptions.purchaseToken, purchaseToken));
  } catch (err) {
    if (err instanceof PlayConfigError || err instanceof PlayUpstreamError) {
      // Non-2xx makes Pub/Sub redeliver, which is what we want on transient failures.
      return apiError("upstream_error", "Could not refresh subscription yet.");
    }
    throw err;
  }
  return apiOk({ ok: true });
}
