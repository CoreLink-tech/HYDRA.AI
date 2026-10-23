import { and, asc, eq, inArray } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { env } from "./env";

/** Version of the public catalogue contract. Bump when the response shape changes. */
export const CATALOG_API_VERSION = 1;

export type CatalogModel = {
  id: string;
  name: string;
  publisher: string;
  tier: "fast" | "balanced" | "advanced";
  version: string;
  description: string;
  intendedUse: string;
  strengths: string[];
  limitations: string[];
  format: string;
  quantization: string;
  downloadSizeBytes: number;
  storageSizeBytes: number;
  minRamMb: number;
  minAndroidSdk: number;
  license: { name: string; url: string };
  sourceUrl: string;
  sha256: string;
  requiresPro: boolean;
  /** Null when the caller is not entitled to this model (Pro-only, free user). */
  downloadUrl: string | null;
  updatedAt: number;
};

export function r2UrlFor(r2Key: string): string {
  const key = r2Key.replace(/^\/+/, "");
  return `${env.r2PublicBaseUrl()}/${key.split("/").map(encodeURIComponent).join("/")}`;
}

function lines(value: string): string[] {
  return value
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean);
}

/**
 * Published catalogue only. Draft and revoked rows never leave the server.
 * `entitledToPro` decides whether Pro-only download URLs are included.
 */
export async function getPublishedCatalog(entitledToPro: boolean): Promise<CatalogModel[]> {
  const rows = await getDb()
    .select()
    .from(schema.modelMetadata)
    .where(
      and(
        eq(schema.modelMetadata.status, "published"),
        inArray(schema.modelMetadata.tier, ["fast", "balanced", "advanced"]),
      ),
    )
    .orderBy(asc(schema.modelMetadata.sortOrder), asc(schema.modelMetadata.id));

  return rows.map((m) => {
    const allowed = !m.requiresPro || entitledToPro;
    return {
      id: m.id,
      name: m.name,
      publisher: m.publisher,
      tier: m.tier,
      version: m.version,
      description: m.description,
      intendedUse: m.intendedUse,
      strengths: lines(m.strengths),
      limitations: lines(m.limitations),
      format: m.format,
      quantization: m.quantization,
      downloadSizeBytes: m.downloadSizeBytes,
      storageSizeBytes: m.storageSizeBytes,
      minRamMb: m.minRamMb,
      minAndroidSdk: m.minAndroidSdk,
      license: { name: m.licenseName, url: m.licenseUrl },
      sourceUrl: m.sourceUrl,
      sha256: m.sha256,
      requiresPro: m.requiresPro,
      // TODO: Pro-only files need signed/expiring R2 URLs (S3 presign with R2 API token).
      // Until then a public R2 URL is only handed out to entitled callers, not enforced at the bucket.
      downloadUrl: allowed ? r2UrlFor(m.r2Key) : null,
      updatedAt: m.updatedAt,
    };
  });
}
