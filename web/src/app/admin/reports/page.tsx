import { desc, eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { resolveReport } from "./actions";

export const dynamic = "force-dynamic";

export default async function ReportsPage({ searchParams }: { searchParams: Promise<{ status?: string }> }) {
  const { status } = await searchParams;
  const filter = ["open", "reviewing", "dismissed", "actioned"].includes(status ?? "") ? status! : "all";
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
    .where(filter === "all" ? undefined : eq(schema.abuseReports.status, filter as "open"))
    .orderBy(desc(schema.abuseReports.createdAt))
    .limit(200);

  return (
    <div>
      <h1 className="text-2xl font-semibold">Abuse reports</h1>
      <p className="mt-1 text-sm text-fg-3">
        Hydra cannot read shared chats. Review the reason and reporter details, then take down the share if it breaks policy.
      </p>
      <nav aria-label="Filter by status" className="mt-4 flex flex-wrap gap-2 text-sm">
        {["all", "open", "reviewing", "dismissed", "actioned"].map((f) => (
          <a key={f} href={f === "all" ? "/admin/reports" : `/admin/reports?status=${f}`} aria-current={filter === f ? "page" : undefined} className={`min-h-9 rounded-btn border px-3 py-1.5 capitalize ${filter === f ? "border-accent bg-accent-soft text-accent" : "border-line-strong text-fg-2 hover:text-fg"}`}>{f}</a>
        ))}
      </nav>
      <div className="mt-4 space-y-3">
        {rows.map((r) => (
          <div key={r.id} className="rounded-card border border-line bg-card p-5">
            <div className="flex flex-wrap items-center gap-3 text-sm">
              <span className="rounded-full bg-warn-soft px-2 py-0.5 text-xs font-medium text-warn">{r.status}</span>
              <span className="font-medium">{r.reason.replace(/_/g, " ")}</span>
              <span className="text-fg-3">{new Date(r.createdAt).toISOString().slice(0, 16).replace("T", " ")} UTC</span>
              <span className="font-mono text-xs text-fg-3">share {r.shareId}</span>
              {r.takedownAt || r.revokedAt ? <span className="text-xs text-danger">{r.takedownAt ? "taken down" : "revoked by sender"}</span> : null}
            </div>
            {r.details ? <p className="mt-2 whitespace-pre-wrap text-sm text-fg-2">{r.details}</p> : null}
            {r.status === "open" || r.status === "reviewing" ? (
              <form
                className="mt-4 flex flex-wrap items-center gap-2"
                action={async (fd: FormData) => {
                  "use server";
                  const decision = String(fd.get("decision")) as "reviewing" | "dismissed" | "actioned";
                  await resolveReport(r.id, decision, String(fd.get("note") ?? ""));
                }}
              >
                <input name="note" placeholder="Admin note" className="w-64 rounded-btn border border-line-strong px-3 py-1.5 text-sm" />
                <button name="decision" value="reviewing" className="rounded-btn border border-line-strong px-3 py-1.5 text-sm">Mark reviewing</button>
                <button name="decision" value="dismissed" className="rounded-btn border border-line-strong px-3 py-1.5 text-sm">Dismiss</button>
                <button name="decision" value="actioned" className="rounded-btn bg-danger px-3 py-1.5 text-sm text-on-accent">Take down share</button>
              </form>
            ) : null}
          </div>
        ))}
        {rows.length === 0 ? <p className="text-sm text-fg-3">No reports.</p> : null}
      </div>
    </div>
  );
}
