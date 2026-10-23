"use server";

import { revalidatePath } from "next/cache";
import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

type Decision = "reviewing" | "dismissed" | "actioned";

/**
 * Resolves an abuse report. "actioned" takes the share down: the blob is deleted and the share
 * marked taken down, so recipients get 410. Admin never sees plaintext because none is stored.
 */
export async function resolveReport(reportId: string, decision: Decision, note: string) {
  const db = getDb();
  const [report] = await db
    .select({ shareId: schema.abuseReports.shareId })
    .from(schema.abuseReports)
    .where(eq(schema.abuseReports.id, reportId))
    .limit(1);
  if (!report) return;

  const now = Date.now();
  await db
    .update(schema.abuseReports)
    .set({
      status: decision,
      adminNote: note.slice(0, 1000) || null,
      resolvedAt: decision === "reviewing" ? null : now,
    })
    .where(eq(schema.abuseReports.id, reportId));

  if (decision === "actioned") {
    const [share] = await db
      .select({ blobId: schema.shareRecords.blobId })
      .from(schema.shareRecords)
      .where(eq(schema.shareRecords.id, report.shareId))
      .limit(1);
    await db.update(schema.shareRecords).set({ takedownAt: now }).where(eq(schema.shareRecords.id, report.shareId));
    if (share) await db.delete(schema.shareBlobs).where(eq(schema.shareBlobs.id, share.blobId));
  }
  revalidatePath("/admin/reports");
}
