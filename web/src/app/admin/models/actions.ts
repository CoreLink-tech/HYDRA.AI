"use server";

import { revalidatePath } from "next/cache";
import { eq } from "drizzle-orm";
import { getDb, schema } from "@/db/client";

const TIERS = ["fast", "balanced", "advanced"] as const;
const STATUSES = ["draft", "published", "revoked"] as const;
const SHA256_RE = /^[a-f0-9]{64}$/;

function fieldStr(form: FormData, key: string): string {
  const v = form.get(key);
  return typeof v === "string" ? v.trim() : "";
}
function fieldInt(form: FormData, key: string): number {
  const n = Number.parseInt(fieldStr(form, key), 10);
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

/** Create or update a catalogue entry. Validation happens here, not only in the UI. */
export async function saveModel(form: FormData): Promise<{ ok: boolean; message: string }> {
  const id = fieldStr(form, "id");
  const tier = fieldStr(form, "tier") as (typeof TIERS)[number];
  const status = fieldStr(form, "status") as (typeof STATUSES)[number];
  const sha256 = fieldStr(form, "sha256").toLowerCase();
  const values = {
    id,
    name: fieldStr(form, "name"),
    publisher: fieldStr(form, "publisher"),
    tier,
    version: fieldStr(form, "version"),
    description: fieldStr(form, "description"),
    intendedUse: fieldStr(form, "intendedUse"),
    strengths: fieldStr(form, "strengths"),
    limitations: fieldStr(form, "limitations"),
    format: "GGUF",
    quantization: fieldStr(form, "quantization"),
    downloadSizeBytes: fieldInt(form, "downloadSizeBytes"),
    storageSizeBytes: fieldInt(form, "storageSizeBytes"),
    minRamMb: fieldInt(form, "minRamMb"),
    minAndroidSdk: fieldInt(form, "minAndroidSdk") || 29,
    licenseName: fieldStr(form, "licenseName"),
    licenseUrl: fieldStr(form, "licenseUrl"),
    commercialUseReviewed: form.get("commercialUseReviewed") === "on",
    sourceUrl: fieldStr(form, "sourceUrl"),
    r2Key: fieldStr(form, "r2Key"),
    sha256,
    requiresPro: form.get("requiresPro") === "on",
    status,
    sortOrder: fieldInt(form, "sortOrder"),
    updatedAt: Date.now(),
  };

  if (!/^[a-z0-9][a-z0-9-]{2,63}$/.test(id)) return { ok: false, message: "id: lowercase slug, 3 to 64 chars." };
  if (!TIERS.includes(tier)) return { ok: false, message: "tier is invalid." };
  if (!STATUSES.includes(status)) return { ok: false, message: "status is invalid." };
  if (!/^\d+\.\d+\.\d+/.test(values.version)) return { ok: false, message: "version must look like 1.0.0." };
  if (!SHA256_RE.test(sha256)) return { ok: false, message: "sha256 must be 64 lowercase hex characters." };
  if (!/^https:\/\//.test(values.licenseUrl) || !/^https:\/\//.test(values.sourceUrl)) {
    return { ok: false, message: "licenseUrl and sourceUrl must be https links." };
  }
  if (!values.r2Key || values.r2Key.startsWith("/") || values.r2Key.includes("..")) {
    return { ok: false, message: "r2Key must be a relative object key." };
  }
  if (values.name.length === 0 || values.publisher.length === 0 || values.licenseName.length === 0) {
    return { ok: false, message: "name, publisher and licence are required." };
  }
  // Licensing discipline (product spec 9): no model goes live without a recorded commercial-use review.
  if (status === "published" && !values.commercialUseReviewed) {
    return { ok: false, message: "Tick 'commercial use reviewed' before publishing." };
  }
  if (status === "published" && (values.downloadSizeBytes === 0 || values.minRamMb === 0)) {
    return { ok: false, message: "Set download size and minimum RAM before publishing." };
  }

  const db = getDb();
  const existing = await db.select({ id: schema.modelMetadata.id }).from(schema.modelMetadata).where(eq(schema.modelMetadata.id, id)).limit(1);
  if (existing[0]) {
    await db.update(schema.modelMetadata).set(values).where(eq(schema.modelMetadata.id, id));
  } else {
    await db.insert(schema.modelMetadata).values(values);
  }
  revalidatePath("/admin/models");
  return { ok: true, message: `Saved ${id}.` };
}

export async function setModelStatus(id: string, status: (typeof STATUSES)[number]) {
  if (!STATUSES.includes(status)) return;
  await getDb()
    .update(schema.modelMetadata)
    .set({ status, updatedAt: Date.now() })
    .where(eq(schema.modelMetadata.id, id));
  revalidatePath("/admin/models");
}
