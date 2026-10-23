import { desc, eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

export const dynamic = "force-dynamic";

function fmt(ts: number | null | undefined) {
  return ts ? new Date(ts).toISOString().slice(0, 10) : "-";
}

/** Shows account metadata and billing state only. Chat content is never stored, so there is none to show. */
export default async function UsersPage() {
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
    .orderBy(desc(schema.users.createdAt))
    .limit(500);

  return (
    <div>
      <h1 className="text-2xl font-semibold">Users & subscriptions</h1>
      <p className="mt-1 text-sm text-zinc-500">Latest 500 accounts. Install-only accounts show as anonymous. Content is never stored.</p>
      <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-50 text-zinc-500">
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
              <tr key={`${u.id}-${i}`} className="border-t border-zinc-100">
                <td className="px-4 py-2">{u.email ?? <span className="text-zinc-500">anonymous install</span>}<div className="font-mono text-xs text-zinc-400">{u.id}</div></td>
                <td className="px-4 py-2">{u.role} / {u.status}</td>
                <td className="px-4 py-2">{u.productId ?? "-"}</td>
                <td className="px-4 py-2">{u.subStatus ? `${u.plan}: ${u.subStatus}` : "free"}</td>
                <td className="px-4 py-2">{fmt(u.expiresAt)}</td>
                <td className="px-4 py-2">{fmt(u.createdAt)}</td>
                <td className="px-4 py-2">{fmt(u.lastSeenAt)}</td>
              </tr>
            ))}
            {rows.length === 0 ? <tr><td colSpan={7} className="px-4 py-6 text-center text-zinc-500">No users yet.</td></tr> : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
