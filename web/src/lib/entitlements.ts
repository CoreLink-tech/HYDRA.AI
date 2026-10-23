import { and, desc, eq, inArray } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

export type Plan = "free" | "pro";

/** Subscription states that still grant Pro access (grace and active; on_hold does not). */
const ENTITLED_STATUSES = ["active", "grace"] as const;

export type EntitlementInfo = {
  plan: Plan;
  status: string | null;
  productId: string | null;
  expiresAt: number | null;
  autoRenewing: boolean;
};

/**
 * Pro is granted only by a verified Google Play purchase record that is still within its
 * expiry window. Core offline chat never depends on this: already downloaded models keep working.
 */
export async function getEntitlement(userId: string): Promise<EntitlementInfo> {
  const db = getDb();
  const rows = await db
    .select()
    .from(schema.subscriptions)
    .where(
      and(
        eq(schema.subscriptions.userId, userId),
        inArray(schema.subscriptions.status, [...ENTITLED_STATUSES]),
      ),
    )
    .orderBy(desc(schema.subscriptions.expiresAt))
    .limit(1);

  const sub = rows[0];
  const live = sub && (sub.expiresAt === null || sub.expiresAt > Date.now());
  if (!sub || !live) {
    return { plan: "free", status: sub?.status ?? null, productId: sub?.productId ?? null, expiresAt: sub?.expiresAt ?? null, autoRenewing: false };
  }
  return {
    plan: "pro",
    status: sub.status,
    productId: sub.productId,
    expiresAt: sub.expiresAt,
    autoRenewing: sub.autoRenewing,
  };
}

export async function isPro(userId: string): Promise<boolean> {
  return (await getEntitlement(userId)).plan === "pro";
}

/** Per-plan cap on simultaneously live (non-revoked) shares. */
export const SHARE_LIMITS: Record<Plan, number> = { free: 10, pro: 500 };
