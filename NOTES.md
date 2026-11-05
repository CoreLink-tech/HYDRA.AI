Copyright (c) 2026 David Ashe. See LICENSE.

## Status

**v0.1.0 engineering alpha** — in-repo foundation complete. External ops (R2, Play products, staging relay, real DSN, upload keystore) remain outside this repository.

# Engineering Log – Hydra.AI

## Decisions

- Offline chat with a downloaded model is **never** gated by subscription (`EntitlementPolicy`).
- Share keys live only in the URL fragment (`#k=...`); the relay is zero-knowledge.
- Local database uses SQLCipher with a passphrase wrapped by an Android Keystore AES-GCM key.
- Manual composition root in `HydraApp` (Hilt deferred to a later phase).
- Model catalogue ships in the app binary for offline first-run; can later be refreshed from the web API.
- Billing defaults to `MockBillingGateway` while `BuildConfig.USE_MOCK_BILLING` is true so Settings can observe a live entitlement flow without Play Console products.
- Release signing is optional and driven by a local `keystore.properties` file that is never committed.

## Phase progress

### Phase 1 (done)
- LICENSE (BSL 1.1), rewritten README, SECURITY.md, CONTRIBUTING.md, ARCHITECTURE.md
- GitHub Actions CI (unit tests + web typecheck)
- Repository description + topics

### Phase 2 (done for in-repo scope)
- `HydraApp.billing` + Settings observes live entitlement / share capacity
- Additional unit tests and instrumented smoke scaffold
- Remaining: real Play Activity provider, Compose journeys, server catalogue

### Phase 3 (done for in-repo scope)
- Expanded ARCHITECTURE.md, CHANGELOG.md, RELEASE.md, keystore template, alpha version, GitHub Release
- External remaining: R2, Play Console, staging deploy, real upload keystore

### Phase 4 (advanced – in-repo foundation done)
- **Done:** `AppContainer` interface; `HydraApp` implements it (path toward Hilt)
- **Done:** Nearby NoOp + Play skeleton + unit test
- **Done:** Consent-gated crash reporter in composition root
- **Done:** `ModelCatalogSource` abstraction
- **Done:** Detekt config + CI job (`ignoreFailures=true` for alpha; tighten later with a baseline)
- **Done:** Compose UI tests for offline banner (`AppRouterComposeTest`)
- **Still open:** full Play Nearby ConnectionsClient, Sentry/Crashlytics DSN, full Hilt factory migration

## Known limitations / stubs

Nothing below is fully configured for a production release. Each item must be completed before a public release build works end-to-end.

### Play Billing and Play Console
- Play service account credentials (`GOOGLE_PLAY_SERVICE_ACCOUNT_*`) are not set (see `web/.env.example`).
- Package name and subscription product IDs are not created in Play Console.
- `core:billing` uses a mock gateway in dev builds (`USE_MOCK_BILLING=true`).
- Server-side purchase verification lives under `web /api/v1/billing`.
- When flipping `USE_MOCK_BILLING` to false, supply a real `PlayBillingGateway` with an Activity provider for the purchase sheet.

### R2 / object storage
- `R2_PUBLIC_BASE_URL` is not set. `core:download` cannot fetch production models until it is configured and checksums are filled in.

### Relay
- Online share relay (`web/src/app/api/v1/shares`) is not deployed.
- `NEXT_PUBLIC_SITE_URL` and the app's `RELAY_BASE_URL` are empty (offline-only builds).
- Nearby: `NoOpNearbyShareTransport` is default; `PlayNearbyShareTransport` is a fail-closed skeleton until on-device verification.
- Settings share capacity currently reports `0 of N` live shares until the relay can list them.

### Email / auth
- Account email verification is not wired. No mail provider is configured.
- `AUTH_SECRET` is required by the web auth layer.

### RTDN
- Real-time developer notifications route needs `PLAY_RTDN_SECRET` (or preferably Pub/Sub OIDC verification).

### Crash reporting
- `core:monitoring` has no backend DSN. Opt-in reports currently go nowhere.

### Feature modules and app wiring
- Feature screens exist and unit-test their pure logic; the UI itself has not been fully verified on a physical device.
- Full `assembleDebug` requires `bash scripts/vendor-llama.sh` first (native sources are not committed).
- Wi-Fi-only downloads use WorkManager's `UNMETERED` constraint.

### Downloads
- `core:download` has a real OkHttp resumable fetcher, SHA-256 verification, and a WorkManager worker. It still needs a production base URL and real checksums before models can be downloaded end-to-end against R2.

### Signing
- Release signing activates only when `keystore.properties` exists locally. See `keystore.properties.example` and `RELEASE.md`.

## Technical debt

- Model catalogue still points at Hugging Face URLs for development
- Full Compose UI journeys beyond offline-banner tests
- RTDN still uses a shared-secret query parameter (TODO for OIDC)
- ViewModel factories still manual (AppContainer ready for Hilt)
- `USE_MOCK_BILLING=false` path still falls back to mock until Activity provider is wired

## Future work

- Optional full Hilt migration of ViewModel factories
- Complete Play Nearby on-device implementation
- Production crash backend (Sentry or Crashlytics) behind existing consent + Scrubber
- External ops: R2, Play Console, staging/production web deploy
