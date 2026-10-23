import { desc, eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { resolveReport } from "./actions";

export const dynamic = "force-dynamic";

export default async function ReportsPage() {
  const rows = await getDb()
    .select({
      id: schema.abuseReports.id,
      shareId: schema.abuseReports.shareId,
      reason: schema.abuseReports.reason,
      details: schema.abuseReports.details,
      status: schema.abuseReports.status,
      createdAt: schema.abuseReports.createdAt,
      takedownAt: schema.shareRecords.takedownAt,
      revokedAt: schema.shareRecords.revokedAt,
    })
    .from(schema.abuseReports)
    .leftJoin(schema.shareRecords, eq(schema.shareRecords.id, schema.abuseReports.shareId))
    .orderBy(desc(schema.abuseReports.createdAt))
    .limit(200);

  return (
    <div>
      <h1 className="text-2xl font-semibold">Abuse reports</h1>
      <p className="mt-1 text-sm text-zinc-500">
        Hydra cannot read shared chats. Review the reason and reporter details, then take down the share if it breaks policy.
      </p>
      <div className="mt-4 space-y-3">
        {rows.map((r) => (
          <div key={r.id} className="rounded-xl border border-zinc-200 bg-white p-5">
            <div className="flex flex-wrap items-center gap-3 text-sm">
              <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">{r.status}</span>
              <span className="font-medium">{r.reason.replace(/_/g, " ")}</span>
              <span className="text-zinc-500">{new Date(r.createdAt).toISOString().slice(0, 16).replace("T", " ")} UTC</span>
              <span className="font-mono text-xs text-zinc-400">share {r.shareId}</span>
              {r.takedownAt || r.revokedAt ? <span className="text-xs text-red-600">{r.takedownAt ? "taken down" : "revoked by sender"}</span> : null}
            </div>
            {r.details ? <p className="mt-2 whitespace-pre-wrap text-sm text-zinc-700">{r.details}</p> : null}
            {r.status === "open" || r.status === "reviewing" ? (
              <form
                className="mt-4 flex flex-wrap items-center gap-2"
                action={async (fd: FormData) => {
                  "use server";
                  const decision = String(fd.get("decision")) as "reviewing" | "dismissed" | "actioned";
                  await resolveReport(r.id, decision, String(fd.get("note") ?? ""));
                }}
              >
                <input name="note" placeholder="Admin note" className="w-64 rounded-md border border-zinc-300 px-3 py-1.5 text-sm" />
                <button name="decision" value="reviewing" className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm">Mark reviewing</button>
                <button name="decision" value="dismissed" className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm">Dismiss</button>
                <button name="decision" value="actioned" className="rounded-md bg-red-600 px-3 py-1.5 text-sm text-white">Take down share</button>
              </form>
            ) : null}
          </div>
        ))}
        {rows.length === 0 ? <p className="text-sm text-zinc-500">No reports.</p> : null}
      </div>
    </div>
  );
}
