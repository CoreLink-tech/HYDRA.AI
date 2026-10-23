/** Shared limits for the encrypted share relay. The server never inspects ciphertext. */
export const MAX_CIPHERTEXT_BYTES = 2 * 1024 * 1024; // 2 MiB of decoded ciphertext
export const MAX_SHARE_TTL_HOURS = 24 * 90;
export const MAX_MESSAGES_PER_SHARE = 2000;
export const SHARE_MODES = ["full", "selected", "read_only", "continuable"] as const;
export type ShareMode = (typeof SHARE_MODES)[number];

const B64 = /^[A-Za-z0-9+/]+={0,2}$/;

/**
 * Validates base64 and returns the decoded byte length, or null when it is not valid base64.
 * Only base64 is accepted so the relay cannot be used as a generic file host.
 */
export function decodedLength(b64: string): number | null {
  if (b64.length === 0 || b64.length % 4 !== 0 || !B64.test(b64)) return null;
  const padding = b64.endsWith("==") ? 2 : b64.endsWith("=") ? 1 : 0;
  return (b64.length / 4) * 3 - padding;
}

export const REPORT_REASONS = [
  "illegal",
  "harassment",
  "sexual_content",
  "malware_or_scam",
  "privacy",
  "other",
] as const;
