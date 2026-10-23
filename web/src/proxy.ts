import { NextResponse, type NextRequest } from "next/server";

/**
 * Basic auth gate for /admin and the admin API. Credentials come from ADMIN_USER / ADMIN_PASSWORD.
 * If either is unset, the admin area is closed entirely (fails closed).
 *
 * TODO(security): replace basic auth with SSO (Cloudflare Access) before opening to a team.
 */
export function proxy(req: NextRequest) {
  const user = process.env.ADMIN_USER ?? "";
  const pass = process.env.ADMIN_PASSWORD ?? "";
  if (!user || !pass) {
    return new NextResponse("Admin is not configured.", { status: 503 });
  }

  const header = req.headers.get("authorization") ?? "";
  const [scheme, encoded] = header.split(" ");
  if (scheme === "Basic" && encoded) {
    let decoded = "";
    try {
      decoded = atob(encoded);
    } catch {
      decoded = "";
    }
    const idx = decoded.indexOf(":");
    if (idx > 0 && decoded.slice(0, idx) === user && decoded.slice(idx + 1) === pass) {
      return NextResponse.next();
    }
  }
  return new NextResponse("Authentication required.", {
    status: 401,
    headers: { "WWW-Authenticate": 'Basic realm="Hydra admin", charset="UTF-8"' },
  });
}

export const config = {
  matcher: ["/admin/:path*"],
};
