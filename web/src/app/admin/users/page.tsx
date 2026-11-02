import { and, desc, eq, like, or } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

export const dynamic = "force-dynamic";

function fmt(ts: number | null | undefined) {
  return ts ? new Date(ts).toISOString().slice(0, 10) : "-";
}

/** Shows account metadata and billing state only. Chat content is never stored, so there is none to show. */
export default async function UsersPage({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const { q } = await searchParams;
  const term = (q ?? "").trim().slice(0, 100).replace(/[%_]/g, "");
  const db = getDb();
  const rows = await db
    .select({
      id: schema.users.id,
      email: schema.users.email,
      role: schema.users.role,
      status: schema.users.status,
      createdAt: schema.users.createdAt,
      lastSeenAt: schema.users.lastSeenAt,
      plan: schema.subscriptions.plan,
      subStatus: schema.subscriptions.status,
      productId: schema.subscriptions.productId,
      expiresAt: schema.subscriptions.expiresAt,
    })
    .from(schema.users)
    .leftJoin(schema.subscriptions, eq(schema.subscriptions.userId, schema.users.id))
    .where(term ? or(like(schema.users.email, `%${term}%`), like(schema.users.id, `%${term}%`)) : undefined)
    .orderBy(desc(schema.users.createdAt))
    .limit(500);

  return (
    <div>
      <h1 className="text-2xl font-semibold">Users & subscriptions</h1>
      <p className="mt-1 text-sm text-fg-3">Latest 500 accounts. Install-only accounts show as anonymous. Content is never stored.</p>
      <form className="mt-4 flex gap-2" role="search">
        <input name="q" defaultValue={term} placeholder="Search by email or account id" aria-label="Search users" className="min-h-11 w-full max-w-sm rounded-btn border border-line-strong bg-card px-3 text-sm text-fg" />
        <button className="min-h-11 rounded-btn border border-line-strong bg-elevated px-4 text-sm font-medium">Search</button>
      </form>
      <div className="mt-4 overflow-x-auto rounded-card border border-line bg-card">
        <table className="w-full text-left text-sm">
          <thead className="bg-surface text-fg-3">
            <tr>
              <th className="px-4 py-2">Account</th>
              <th className="px-4 py-2">Role / status</th>
              <th className="px-4 py-2">Product</th>
              <th className="px-4 py-2">Subscription</th>
              <th className="px-4 py-2">Expires</th>
              <th className="px-4 py-2">Created</th>
              <th className="px-4 py-2">Last seen</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((u, i) => (
              <tr key={`${u.id}-${i}`} className="border-t border-line">
                <td className="px-4 py-2">{u.email ?? <span className="text-fg-3">anonymous install</span>}<div className="font-mono text-xs text-fg-3">{u.id}</div></td>
                <td className="px-4 py-2">{u.role} / {u.status}</td>
                <td className="px-4 py-2">{u.productId ?? "-"}</td>
                <td className="px-4 py-2">{u.subStatus ? `${u.plan}: ${u.subStatus}` : "free"}</td>
                <td className="px-4 py-2">{fmt(u.expiresAt)}</td>
                <td className="px-4 py-2">{fmt(u.createdAt)}</td>
                <td className="px-4 py-2">{fmt(u.lastSeenAt)}</td>
              </tr>
            ))}
            {rows.length === 0 ? <tr><td colSpan={7} className="px-4 py-6 text-center text-fg-3">No matching users.</td></tr> : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
