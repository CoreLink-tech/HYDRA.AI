# Engineering Log – Hydra.AI

## Decisions

- Offline chat with a downloaded model is **never** gated by subscription (`EntitlementPolicy`).
- Share keys live only in the URL fragment (`#k=...`); the relay is zero-knowledge.
- Local database uses SQLCipher with a passphrase wrapped by an Android Keystore AES-GCM key.
- Manual composition root in `HydraApp` (Hilt deferred to a later phase).
- Model catalogue ships in the app binary for offline first-run; can later be refreshed from the web API.

## Known limitations / stubs

Nothing below is fully configured for a production release. Each item must be completed before a public release build works end-to-end.

### Play Billing and Play Console
- Play service account credentials (`GOOGLE_PLAY_SERVICE_ACCOUNT_*`) are not set (see `web/.env.example`).
- Package name and subscription product IDs are not created in Play Console.
- `core:billing` uses a mock gateway in dev builds (`USE_MOCK_BILLING=true`).
- Server-side purchase verification lives under `web /api/v1/billing`.

### R2 / object storage
- `R2_PUBLIC_BASE_URL` is not set. `core:download` cannot fetch production models until it is configured and checksums are filled in.

### Relay
- Online share relay (`web/src/app/api/v1/shares`) is not deployed.
- `NEXT_PUBLIC_SITE_URL` and the app's `RELAY_BASE_URL` are empty (offline-only builds).
- Nearby Connections transport is an interface only; the Google Play Services implementation is not written.

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
- **Entitlement is still hard-coded to `Plan.FREE` in `MainActivity`** (marked with a Phase-1 comment). Replace with live observation from `BillingGateway` before release.
- Share capacity line in Settings is an empty placeholder until the relay is live.
- Wi-Fi-only downloads use WorkManager's `UNMETERED` constraint.

### Downloads
- `core:download` has a real OkHttp resumable fetcher, SHA-256 verification, and a WorkManager worker. It still needs a production base URL and real checksums in the catalogue before models can be downloaded end-to-end.

## Technical debt

- Hard-coded FREE entitlement in `MainActivity`
- Nearby transport is interface-only
- Model catalogue still points at Hugging Face URLs for development
- No instrumented / UI tests yet
- RTDN still uses a shared-secret query parameter (TODO for OIDC)
- Manual DI / ViewModel factories in the app module

## Future work (aligned with improvement roadmap)

- Phase 2: real entitlement wiring, expanded tests, dependency bumps
- Phase 3: full documentation set, release process, staging deployment
- Phase 4: Hilt, Nearby implementation, production crash reporting

