/**
 * Typed access to environment variables. Missing values throw only when the feature is used,
 * so the marketing site still builds and renders without every secret set.
 */
function required(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`${name} is not set. See web/.env.example.`);
  }
  return value;
}

export const env = {
  siteUrl: process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000",
  r2PublicBaseUrl: () => required("R2_PUBLIC_BASE_URL").replace(/\/+$/, ""),
  authSecret: () => {
    const secret = required("AUTH_SECRET");
    if (secret.length < 32) throw new Error("AUTH_SECRET must be at least 32 characters.");
    return secret;
  },
  adminUser: () => process.env.ADMIN_USER ?? "",
  adminPassword: () => process.env.ADMIN_PASSWORD ?? "",
  playPackageName: () => process.env.GOOGLE_PLAY_PACKAGE_NAME ?? "com.zenlink.hydra",
  playServiceAccountEmail: () => process.env.GOOGLE_PLAY_SERVICE_ACCOUNT_EMAIL ?? "",
  playServiceAccountKey: () =>
    (process.env.GOOGLE_PLAY_SERVICE_ACCOUNT_PRIVATE_KEY ?? "").replace(/\\n/g, "\n"),
};
