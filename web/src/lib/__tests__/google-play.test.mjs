import assert from "node:assert/strict";
import { test } from "node:test";

// Pure mapping mirrored from google-play.ts (kept in sync by review).
// Prefer importing the TS module once deps install; this pure copy keeps CI offline-friendly.
function mapPlayState(state) {
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

function mockFetchSubscription(purchaseToken) {
  const active = !purchaseToken.includes("expired");
  return {
    subscriptionState: active ? "SUBSCRIPTION_STATE_ACTIVE" : "SUBSCRIPTION_STATE_EXPIRED",
    lineItems: [{ productId: "hydra_pro_monthly" }],
  };
}

test("mapPlayState maps active and expired", () => {
  assert.equal(mapPlayState("SUBSCRIPTION_STATE_ACTIVE"), "active");
  assert.equal(mapPlayState("SUBSCRIPTION_STATE_EXPIRED"), "expired");
  assert.equal(mapPlayState("SUBSCRIPTION_STATE_IN_GRACE_PERIOD"), "grace");
  assert.equal(mapPlayState("SUBSCRIPTION_STATE_UNSPECIFIED"), "pending");
});

test("mockFetchSubscription returns active for normal tokens", () => {
  const sub = mockFetchSubscription("tok-abc");
  assert.equal(sub.subscriptionState, "SUBSCRIPTION_STATE_ACTIVE");
  assert.equal(sub.lineItems[0].productId, "hydra_pro_monthly");
});

test("mockFetchSubscription returns expired when token contains expired", () => {
  const sub = mockFetchSubscription("tok-expired-1");
  assert.equal(sub.subscriptionState, "SUBSCRIPTION_STATE_EXPIRED");
});
