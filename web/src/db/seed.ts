import { getDb, schema } from "./client";

/**
 * Seeds one clearly-labelled draft catalogue row so the admin dashboard has something to edit.
 * Draft rows are never returned by the public catalogue endpoint. Real sha256 values and R2 keys
 * are filled in from the admin dashboard after the GGUF file is uploaded and licence review is done.
 * Idempotent: existing ids are left alone.
 */
const seedModels: (typeof schema.modelMetadata.$inferInsert)[] = [
  {
    id: "hydra-fast-placeholder",
    name: "Hydra Fast (placeholder)",
    publisher: "TBD: pending licence review",
    tier: "fast",
    version: "0.0.0",
    description: "Placeholder entry. Replace with a licence-reviewed model before publishing.",
    intendedUse: "Quick chat, summaries, rewriting, basic questions",
    quantization: "Q4_K_M",
    downloadSizeBytes: 0,
    storageSizeBytes: 0,
    minRamMb: 3072,
    licenseName: "TBD",
    licenseUrl: "https://example.com/license",
    sourceUrl: "https://example.com/source",
    r2Key: "models/hydra-fast-placeholder/0.0.0.gguf",
    sha256: "0".repeat(64),
    status: "draft",
    sortOrder: 10,
  },
];

async function main() {
  const db = getDb();
  for (const row of seedModels) {
    await db.insert(schema.modelMetadata).values(row).onConflictDoNothing();
  }
  console.log(`seeded ${seedModels.length} draft model row(s); existing ids untouched`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
