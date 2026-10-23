import { getAuthedUser } from "@/lib/auth";
import { CATALOG_API_VERSION, getPublishedCatalog } from "@/lib/catalog";
import { apiError, apiOk } from "@/lib/http";
import { getEntitlement } from "@/lib/entitlements";

/**
 * GET /api/v1/models
 * Public, versioned model catalogue. Optional Bearer token: a Pro user also gets download URLs
 * for Pro-only models. Draft and revoked models are never returned.
 *
 * The app compares `catalogVersion` and each model's `version`/`sha256` and verifies the
 * downloaded GGUF against sha256 before loading it.
 */
export async function GET(req: Request) {
  let entitledToPro = false;
  if (req.headers.get("authorization")) {
    const user = await getAuthedUser(req);
    if (!user) return apiError("unauthorized", "Invalid token.");
    entitledToPro = (await getEntitlement(user.id)).plan === "pro";
  }

  const models = await getPublishedCatalog(entitledToPro);
  return apiOk(
    {
      apiVersion: CATALOG_API_VERSION,
      catalogVersion: models.map((m) => `${m.id}@${m.version}`).join(","),
      generatedAt: Date.now(),
      models,
    },
    { headers: { "cache-control": "public, max-age=60" } },
  );
}
