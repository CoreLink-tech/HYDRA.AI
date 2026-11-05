# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/).

## [0.1.0] – 2026-11-05

### Build artifacts

Debug APK tags `debug-build-1` … `debug-build-10` publish `hydra-debug.apk` via the Build debug APK workflow (see `.github/workflows/build-apk.yml`). Those tags are CI binary snapshots, not product version bumps.


Engineering alpha suitable for technical review and licensing discussion. Not a production Play Store release.

### Added
- On-device LLM chat architecture (Kotlin, Jetpack Compose, llama.cpp JNI)
- Modular Gradle project: `app`, `core/*`, `feature/*`, `web/`
- SQLCipher-encrypted local database with Android Keystore-wrapped passphrase
- Resumable model downloads with SHA-256 integrity checks (WorkManager)
- End-to-end encrypted share design (AES-256-GCM; key in URL fragment only)
- Free vs Pro entitlement policy (offline chat never gated)
- Mock billing gateway for development; Play Billing client scaffold
- `AppContainer` composition-root interface; Nearby NoOp + Play skeleton
- Consent-gated crash reporter (noop backend until DSN configured)
- `ModelCatalogSource` abstraction for future network catalogue refresh
- Unit tests for routing, crypto, integrity, entitlement, planners, catalogue
- Instrumented smoke test + Compose UI tests for offline banner copy
- Next.js relay / auth / billing verification / admin scaffold under `web/`
- Business Source License 1.1 + LICENSING.md
- SECURITY.md, CONTRIBUTING.md, ARCHITECTURE.md, RELEASE.md, CI workflow
- Optional release signing via `keystore.properties` (never committed)
- Detekt static analysis (alpha: `ignoreFailures=true` + baseline path)

### Known limitations
- `USE_MOCK_BILLING=true` by default; Play products not configured
- Model catalogue still points at public Hugging Face URLs for development
- Share relay and R2 model hosting not deployed
- Full UI not verified on physical devices in CI
- Native llama.cpp sources must be vendored: `bash scripts/vendor-llama.sh`
- Nearby Share and production crash reporting are scaffolds, not production backends

## [0.1.0-alpha] – 2026-11-05

Pre-tag engineering snapshot (superseded by 0.1.0 documentation polish).

## [Unreleased]

### Planned
- Production R2 model hosting and catalogue API
- Play Console products and server-side verification
- Deployed share relay
- Full Play Nearby ConnectionsClient on device
- Production crash backend (Sentry or Crashlytics)
- Detekt baseline enforcement (`ignoreFailures=false`)

[0.1.0]: https://github.com/CoreLink-tech/HYDRA.AI/releases/tag/v0.1.0
[0.1.0-alpha]: https://github.com/CoreLink-tech/HYDRA.AI/releases/tag/v0.1.0-alpha
