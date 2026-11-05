# Engineering Log – Hydra.AI

## Decisions

- Offline chat with a downloaded model is **never** gated by subscription (`EntitlementPolicy`).
- Share keys live only in the URL fragment (`#k=...`); the relay is zero-knowledge.
- Local database uses SQLCipher with a passphrase wrapped by an Android Keystore AES-GCM key.
- Manual composition root in `HydraApp` (Hilt deferred to a later phase).
- Model catalogue ships in the app binary for offline first-run; can later be refreshed from the web API.
- Billing defaults to `MockBillingGateway` while `BuildConfig.USE_MOCK_BILLING` is true so Settings can observe a live entitlement flow without Play Console products.

## Phase progress

### Phase 1 (done)
- LICENSE (BSL 1.1), rewritten README, SECURITY.md, CONTRIBUTING.md, ARCHITECTURE.md
- GitHub Actions CI (unit tests + web typecheck)
- Repository description + topics

### Phase 2 (in progress)
- **Done:** `HydraApp.billing` exposes a `BillingGateway`; Settings observes `entitlement` and shows live share capacity via `SettingsPlanner`
- **Done:** Additional unit tests (`ModelCatalogTest`, expanded `MockBillingGatewayTest`)
- **Done:** Instrumented smoke test scaffold (`SmokeInstrumentedTest`) + androidTest dependencies
- **Still open:** real `PlayBillingGateway` Activity wiring when `USE_MOCK_BILLING=false`
- **Still open:** Compose UI journey tests (require device + vendored llama)
- **Still open:** server-backed model catalogue refresh
- **Still open:** detekt/ktlint formal gate (optional)

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
- Nearby Connections transport is an interface only; the Google Play Services implementation is not written.
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
- `core:download` has a real OkHttp resumable fetcher, SHA-256 verification, and a WorkManager worker. It still needs a production base URL and real checksums in the catalogue before models can be downloaded end-to-end against R2.

## Technical debt

- Nearby transport is interface-only
- Model catalogue still points at Hugging Face URLs for development
- Compose UI / instrumented journey tests not yet written (smoke test only)
- RTDN still uses a shared-secret query parameter (TODO for OIDC)
- Manual DI / ViewModel factories in the app module
- `USE_MOCK_BILLING=false` path still falls back to mock until Activity provider is wired

## Future work

- Phase 3: full documentation polish, release process, staging deployment
- Phase 4: Hilt, Nearby implementation, production crash reporting, detekt gate
