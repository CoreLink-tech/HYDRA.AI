# Release process – Hydra.AI

## Versioning

- **Format:** `MAJOR.MINOR.PATCH` with optional prerelease suffix (`-alpha`, `-beta`, `-rc.N`)
- **Source of truth:** `app/build.gradle.kts` → `versionName` / `versionCode`
- **Changelog:** update [CHANGELOG.md](CHANGELOG.md) before tagging

Current alpha: **0.1.0-alpha** (`versionCode = 1`).

## Prerequisites for a signed release

1. Create an upload keystore **outside** the repository.
2. Copy [keystore.properties.example](keystore.properties.example) to `keystore.properties` (gitignored).
3. Fill in store path, aliases, and passwords. Never commit `keystore.properties` or `*.jks` / `*.keystore`.
4. Set Play Console products (`hydra_pro_monthly`, `hydra_pro_annual`) and service account.
5. Set production `RELAY_BASE_URL` and flip `USE_MOCK_BILLING` to `false` only when Play gateway wiring is complete.
6. Configure `web/.env` on the host (see `web/.env.example`).

## Build commands

```bash
# Always vendor native sources first on a clean machine
bash scripts/vendor-llama.sh

# Unit tests
./gradlew testDebugUnitTest

# Unsigned debug APK
./gradlew assembleDebug

# Signed release APK/AAB (requires keystore.properties)
./gradlew assembleRelease
./gradlew bundleRelease
```

Outputs (when signing is configured):

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## Pre-release checklist

- [ ] CHANGELOG.md updated for this version
- [ ] `versionName` / `versionCode` bumped in `app/build.gradle.kts`
- [ ] `./gradlew testDebugUnitTest` green
- [ ] CI green on `main`
- [ ] No secrets in the tree (`git grep` for tokens, private keys)
- [ ] LICENSE and SECURITY.md present
- [ ] README setup steps still accurate
- [ ] NOTES.md limitations reviewed for honesty
- [ ] Web typecheck: `cd web && npm run typecheck`
- [ ] Staging relay health check (when deployed): `GET /api/v1/health`
- [ ] Tag created: `git tag -a vX.Y.Z -m "..."` and pushed
- [ ] GitHub Release published from the tag

## Staging web (outline)

The `web/` app is a standard Next.js project. A typical staging deploy:

1. Provision Turso (or local libsql) and set `TURSO_*` + `AUTH_SECRET`.
2. Set `NEXT_PUBLIC_SITE_URL` to the staging origin.
3. Deploy to your Node host or Vercel (root directory: `web`).
4. Run `npm run db:migrate` against the staging database.
5. Confirm `GET /api/v1/health` returns ok.
6. Point a debug app build’s `RELAY_BASE_URL` at staging only for integration tests—never commit production secrets.

## Creating the GitHub Release

```bash
git tag -a v0.1.0-alpha -m "v0.1.0-alpha – engineering alpha"
git push origin v0.1.0-alpha
# Then publish via GitHub UI or API with CHANGELOG notes
```

## Rollback

- Prefer a new patch release over rewriting tags that others may have pulled.
- Play Console staged rollouts should be used for production APKs once public testing begins.
