import { NextResponse } from "next/server";

export type ApiErrorCode =
  | "bad_request"
  | "unauthorized"
  | "forbidden"
  | "not_found"
  | "gone"
  | "conflict"
  | "payload_too_large"
  | "quota_exceeded"
  | "not_configured"
  | "upstream_error"
  | "internal";

const STATUS: Record<ApiErrorCode, number> = {
  bad_request: 400,
  unauthorized: 401,
  forbidden: 403,
  not_found: 404,
  gone: 410,
  conflict: 409,
  payload_too_large: 413,
  quota_exceeded: 402,
  not_configured: 503,
  upstream_error: 502,
  internal: 500,
};

export function apiError(code: ApiErrorCode, message: string, extra?: Record<string, unknown>) {
  return NextResponse.json({ error: { code, message, ...extra } }, { status: STATUS[code] });
}

export function apiOk<T>(data: T, init?: { status?: number; headers?: HeadersInit }) {
  return NextResponse.json(data, { status: init?.status ?? 200, headers: init?.headers });
}

/** Reads a JSON object body. Returns null when the body is missing or not an object. */
export async function readJsonObject(req: Request): Promise<Record<string, unknown> | null> {
  try {
    const body: unknown = await req.json();
    if (body && typeof body === "object" && !Array.isArray(body)) {
      return body as Record<string, unknown>;
    }
    return null;
  } catch {
    return null;
  }
}

export function str(
  body: Record<string, unknown>,
  key: string,
  opts: { min?: number; max: number; optional?: boolean },
): string | null | undefined {
  const v = body[key];
  if (v === undefined || v === null) {
    return opts.optional ? undefined : null;
  }
  if (typeof v !== "string") return null;
  const trimmed = v.trim();
  if (trimmed.length < (opts.min ?? 0) || trimmed.length > opts.max) return null;
  return trimmed;
}
