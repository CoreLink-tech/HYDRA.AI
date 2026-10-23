import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { getAuthedUser } from "@/lib/auth";
import { randomId } from "@/lib/crypto";
import { apiError, apiOk, readJsonObject, str } from "@/lib/http";
import { REPORT_REASONS } from "@/lib/shares";

type Ctx = { params: Promise<{ id: string }> };

/**
 * POST /api/v1/shares/:id/report (Bearer optional)
 * body: { reason, details? }. The server cannot read the content, so reports carry the
 * reporter's description. Admins review them in the dashboard and may take down the blob.
 *
 * TODO(abuse): per-user report rate limit.
 */
export async function POST(req: Request, ctx: Ctx) {
  const { id } = await ctx.params;
  const db = getDb();
  const rows = await db
    .select({ id: schema.shareRecords.id })
    .from(schema.shareRecords)
    .where(eq(schema.shareRecords.id, id))
    .limit(1);
  if (!rows[0]) return apiError("not_found", "Share not found.");

  const body = await readJsonObject(req);
  if (!body) return apiError("bad_request", "JSON body required.");
  const reason = body.reason;
  if (typeof reason !== "string" || !(REPORT_REASONS as readonly string[]).includes(reason)) {
    return apiError("bad_request", "reason must be one of " + REPORT_REASONS.join(", ") + ".");
  }
  const details = str(body, "details", { max: 2000, optional: true }) ?? "";

  const reporter = await getAuthedUser(req);
  await db.insert(schema.abuseReports).values({
    id: randomId(12),
    shareId: id,
    reporterUserId: reporter?.id ?? null,
    reason: reason as (typeof REPORT_REASONS)[number],
    details,
  });
  return apiOk({ received: true }, { status: 201 });
}
