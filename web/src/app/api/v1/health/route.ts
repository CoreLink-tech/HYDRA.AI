import { apiOk } from "@/lib/http";

/** GET /api/v1/health: liveness only, no secrets or DB details. */
export function GET() {
  return apiOk({ ok: true, service: "hydra-web" });
}
