import { createSign } from "node:crypto";
import { env } from "./env";

/**
 * Google Play Developer API client (androidpublisher v3), server side.
 *
 * STATUS: stub with real request shapes. It needs a Google Cloud service account that is
 * linked to the Play Console app with "View financial data" and "Manage orders and subscriptions".
 * TODO(credentials): set GOOGLE_PLAY_SERVICE_ACCOUNT_EMAIL and GOOGLE_PLAY_SERVICE_ACCOUNT_PRIVATE_KEY.
 * TODO(testing): verify against a Play license-tester purchase before going live.
 */

const TOKEN_URL = "https://oauth2.googleapis.com/token";
const SCOPE = "https://www.googleapis.com/auth/androidpublisher";
const API = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications";

export type PlaySubscriptionState =
  | "SUBSCRIPTION_STATE_ACTIVE"
  | "SUBSCRIPTION_STATE_IN_GRACE_PERIOD"
  | "SUBSCRIPTION_STATE_ON_HOLD"
  | "SUBSCRIPTION_STATE_PAUSED"
  | "SUBSCRIPTION_STATE_CANCELED"
  | "SUBSCRIPTION_STATE_EXPIRED"
  | "SUBSCRIPTION_STATE_PENDING"
  | "SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED"
  | "SUBSCRIPTION_STATE_UNSPECIFIED";

export type PlaySubscriptionV2 = {
  subscriptionState: PlaySubscriptionState;
  acknowledgementState?: "ACKNOWLEDGEMENT_STATE_PENDING" | "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED";
  latestOrderId?: string;
  startTime?: string;
  lineItems?: {
    productId: string;
    expiryTime?: string;
    autoRenewingPlan?: { autoRenewEnabled?: boolean };
  }[];
};

export class PlayConfigError extends Error {}
export class PlayUpstreamError extends Error {}

function base64url(input: Buffer | string): string {
  return Buffer.from(input).toString("base64url");
}

/** Exchanges a service-account JWT assertion for an OAuth access token. */
async function getAccessToken(): Promise<string> {
  const email = env.playServiceAccountEmail();
  const key = env.playServiceAccountKey();
  if (!email || !key) {
    throw new PlayConfigError("Google Play service account is not configured.");
  }

  const now = Math.floor(Date.now() / 1000);
  const header = base64url(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claims = base64url(
    JSON.stringify({ iss: email, scope: SCOPE, aud: TOKEN_URL, iat: now, exp: now + 3600 }),
  );
  const signer = createSign("RSA-SHA256");
  signer.update(`${header}.${claims}`);
  const signature = signer.sign(key, "base64url");

  const res = await fetch(TOKEN_URL, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: `${header}.${claims}.${signature}`,
    }),
    cache: "no-store",
  });
  if (!res.ok) throw new PlayUpstreamError(`token exchange failed (${res.status})`);
  const json = (await res.json()) as { access_token?: string };
  if (!json.access_token) throw new PlayUpstreamError("token exchange returned no access_token");
  return json.access_token;
}

/** Fetches the authoritative subscription record for a purchase token. Never trust the client. */
export async function fetchSubscription(purchaseToken: string): Promise<PlaySubscriptionV2> {
  const token = await getAccessToken();
  const pkg = env.playPackageName();
  const res = await fetch(
    `${API}/${encodeURIComponent(pkg)}/purchases/subscriptionsv2/tokens/${encodeURIComponent(purchaseToken)}`,
    { headers: { authorization: `Bearer ${token}` }, cache: "no-store" },
  );
  if (!res.ok) throw new PlayUpstreamError(`subscriptionsv2 get failed (${res.status})`);
  return (await res.json()) as PlaySubscriptionV2;
}

/**
 * Acknowledges a subscription so Google does not auto-refund it after ~3 days.
 * TODO(policy): call this only after the purchase is stored, and retry on failure.
 */
export async function acknowledgeSubscription(productId: string, purchaseToken: string): Promise<void> {
  const token = await getAccessToken();
  const pkg = env.playPackageName();
  const res = await fetch(
    `${API}/${encodeURIComponent(pkg)}/purchases/subscriptions/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(purchaseToken)}:acknowledge`,
    {
      method: "POST",
      headers: { authorization: `Bearer ${token}`, "content-type": "application/json" },
      body: "{}",
      cache: "no-store",
    },
  );
  if (!res.ok && res.status !== 409) throw new PlayUpstreamError(`acknowledge failed (${res.status})`);
}

/** Maps Play subscription states onto the subset stored in our subscriptions table. */
export function mapPlayState(state: PlaySubscriptionState): string {
  switch (state) {
    case "SUBSCRIPTION_STATE_ACTIVE":
      return "active";
    case "SUBSCRIPTION_STATE_IN_GRACE_PERIOD":
      return "grace";
    case "SUBSCRIPTION_STATE_ON_HOLD":
      return "on_hold";
    case "SUBSCRIPTION_STATE_PAUSED":
      return "paused";
    case "SUBSCRIPTION_STATE_CANCELED":
      return "canceled";
    case "SUBSCRIPTION_STATE_EXPIRED":
      return "expired";
    default:
      return "pending";
  }
}
