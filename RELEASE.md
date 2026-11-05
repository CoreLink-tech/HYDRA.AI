# Release process – Hydra.AI

## Versioning

- **Format:** `MAJOR.MINOR.PATCH` with optional prerelease suffix
- **Source of truth:** `app/build.gradle.kts` → `versionName` / `versionCode`
- **Changelog:** update [CHANGELOG.md](CHANGELOG.md) before tagging

Current engineering alpha: **0.1.0** (`versionCode = 1`).

## What “v0.1.0” means

This is an **engineering alpha** for technical review and licensing discussion. It is **not** a production Play Store release. Mock billing, undeployed relay, and manual llama vendoring still apply (see README known limitations).

## Prerequisites for a signed binary release

1. Create an upload keystore **outside** the repository.
2. Copy [keystore.properties.example](keystore.properties.example) to `keystore.properties` (gitignored).
3. Fill in store path, aliases, and passwords. Never commit `keystore.properties` or `*.jks`.
4. Set Play Console products and service account when leaving mock billing.
5. Configure `web/.env` on the host (see `web/.env.example`).

## Build commands

```bash
bash scripts/vendor-llama.sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
# Signed (requires keystore.properties):
./gradlew assembleRelease
./gradlew bundleRelease
```

Outputs:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk` (when signed)
- `app/build/outputs/bundle/release/app-release.aab` (when signed)

## Pre-release checklist

- [ ] CHANGELOG.md updated for this version
- [ ] `versionName` / `versionCode` bumped in `app/build.gradle.kts`
- [ ] `./gradlew testDebugUnitTest` green
- [ ] CI green on `main`
- [ ] No secrets in the tree
- [ ] LICENSE, LICENSING.md, SECURITY.md present
- [ ] README known limitations still accurate
- [ ] Tag created and pushed
- [ ] GitHub Release published from the tag

## Publish v0.1.0 (example)

```bash
git tag -a v0.1.0 -m "v0.1.0 – engineering alpha"
git push origin v0.1.0
# Create GitHub Release from the tag with CHANGELOG notes
```

## Staging web (outline)

1. Provision Turso (or local libsql); set `TURSO_*` + `AUTH_SECRET`
2. Set `NEXT_PUBLIC_SITE_URL` to the staging origin
3. Deploy `web/` to your Node host
4. Run `npm run db:migrate`
5. Confirm `GET /api/v1/health`
