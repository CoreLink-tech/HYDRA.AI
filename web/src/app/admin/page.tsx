import { count, eq, isNull, sql } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

export const dynamic = "force-dynamic";

async function stat(q: Promise<{ n: number }[]>) {
  return (await q)[0]?.n ?? 0;
}

export default async function AdminOverview() {
  const db = getDb();
  const now = Date.now();
  const [users, proActive, models, openReports, liveShares] = await Promise.all([
    stat(db.select({ n: count() }).from(schema.users)),
    stat(
      db
        .select({ n: count() })
        .from(schema.subscriptions)
        .where(sql`${schema.subscriptions.status} in ('active','grace') and (${schema.subscriptions.expiresAt} is null or ${schema.subscriptions.expiresAt} > ${now})`),
    ),
    stat(db.select({ n: count() }).from(schema.modelMetadata).where(eq(schema.modelMetadata.status, "published"))),
    stat(db.select({ n: count() }).from(schema.abuseReports).where(eq(schema.abuseReports.status, "open"))),
    stat(
      db
        .select({ n: count() })
        .from(schema.shareRecords)
        .where(isNull(schema.shareRecords.revokedAt)),
    ),
  ]);

  const cards = [
    { label: "Users", value: users },
    { label: "Active Pro", value: proActive },
    { label: "Published models", value: models },
    { label: "Open abuse reports", value: openReports },
    { label: "Non-revoked shares", value: liveShares },
  ];

  return (
    <div>
      <h1 className="text-2xl font-semibold">Overview</h1>
      <div className="mt-6 grid grid-cols-2 gap-4 md:grid-cols-5">
        {cards.map((c) => (
          <div key={c.label} className="rounded-xl border border-zinc-200 bg-white p-5">
            <div className="text-sm text-zinc-500">{c.label}</div>
            <div className="mt-2 text-3xl font-semibold">{c.value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
