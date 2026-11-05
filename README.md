# Hydra.AI

[![CI](https://github.com/CoreLink-tech/HYDRA.AI/actions/workflows/ci.yml/badge.svg)](https://github.com/CoreLink-tech/HYDRA.AI/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/CoreLink-tech/HYDRA.AI?include_prereleases)](https://github.com/CoreLink-tech/HYDRA.AI/releases)
[![License](https://img.shields.io/badge/license-BSL%201.1-blue.svg)](LICENSE)

**Private, on-device AI chat for Android.**

Hydra.AI runs a small language model entirely on your phone via llama.cpp. After the model is downloaded once, chat works offline and nothing you type leaves the device. Optional conversation sharing uses end-to-end encryption: the key is generated on-device and travels only in the link fragment, so the relay stores ciphertext it cannot read.

> **Status: engineering alpha (v0.1.0)**  
> Core modules, feature shells, unit tests, CI, and documentation are in place.  
> Production model hosting, Play Billing products, the share relay deployment, and full on-device UI verification are **not** complete. See [Known limitations](#known-limitations).

## Key features

- Fully offline chat after one model download
- Curated quantized GGUF models (Fast / Balanced / Advanced)
- Device-aware model recommendation (RAM, storage, thermal, ABI)
- Resumable downloads with SHA-256 verification
- End-to-end encrypted conversation sharing (AES-256-GCM design)
- Free vs Pro entitlement policy (**offline chat is never gated**)
- Encrypted local database (SQLCipher + Android Keystore)

## Architecture overview

Modular Gradle project approximating Clean Architecture:

| Layer | Role |
|-------|------|
| `:app` | Composition root (`HydraApp` / `AppContainer`), navigation |
| `:core:model` | Pure domain types (no Android) |
| `:core:*` | Infrastructure (DB, llama, download, billing, sharing, …) |
| `:feature:*` | Compose screens + ViewModels |
| `web/` | Next.js relay, auth, billing verification, admin (separate Node app) |

See [ARCHITECTURE.md](ARCHITECTURE.md) for data flows and security boundaries.

## Module map

| Module | Purpose |
|--------|---------|
| `app` | Navigation, onboarding gate, `HydraApp` |
| `core/model` | Entitlement, ModelDescriptor, ModelCatalog, ModelCatalogSource |
| `core/database` | Room + SQLCipher + KeyProvider + repositories |
| `core/llama` | JNI + LlamaEngine (streaming generation) |
| `core/download` | OkHttp Range + WorkManager + Integrity |
| `core/billing` | BillingGateway, Mock, Play scaffold, EntitlementPolicy |
| `core/sharing` | ShareCrypto, ShareService, HttpShareRelay, Nearby transports |
| `core/device` | DeviceChecker + TierRecommender |
| `core/monitoring` | Consent-gated crash reporting (noop backend until DSN) |
| `core/designsystem` | Material 3 dark theme |
| `feature/*` | Onboarding, Chat, Models, Settings |
| `web` | Next.js 16 + Turso + Drizzle API |

## Technology stack

- Kotlin 2.0, AGP 8.7, Compose BOM 2024.12, Room 2.6, SQLCipher, OkHttp, WorkManager, Play Billing
- llama.cpp (vendored at tag b10232 via script; **not** committed)
- Next.js 16, React 19, Drizzle ORM, Turso/libsql

## Prerequisites

- JDK 17
- Android SDK platform 35
- NDK 27.0.12077973 (Side-by-side)
- CMake 3.22.1
- Node.js ≥ 20.9 (for `web/` only)

## Local development – Android

```bash
git clone https://github.com/CoreLink-tech/HYDRA.AI.git
cd HYDRA.AI

# Required once: fetch llama.cpp sources (not committed)
bash scripts/vendor-llama.sh

# Unit tests (no device or NDK required)
./gradlew testDebugUnitTest

# Optional debug APK (requires NDK + CMake after vendoring)
./gradlew assembleDebug
```

Open the project root in Android Studio and let Gradle sync.

## Local development – Web

```bash
cd web
cp .env.example .env.local   # fill required vars — never commit real values
npm install
npm run db:migrate
npm run dev
```

## Environment variables

See [`web/.env.example`](web/.env.example). Never commit `.env.local`, keystores, or service-account JSON.  
Signing template: [`keystore.properties.example`](keystore.properties.example).

## Test & quality commands

| Goal | Command |
|------|---------|
| Unit tests | `./gradlew testDebugUnitTest` |
| Debug APK | `./gradlew assembleDebug` |
| Release APK/AAB | `./gradlew assembleRelease` / `bundleRelease` (needs `keystore.properties`) |
| Detekt | `./gradlew detekt` |
| Web typecheck | `cd web && npm run typecheck` |
| Web build | `cd web && npm run build` |

Instrumented / Compose UI tests require a device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

## Security notes

- Database passphrase is random, Keystore-wrapped (AES-GCM), never leaves the device
- Share keys exist only in the URL fragment by design
- Cleartext HTTP disabled (`network_security_config.xml`)
- `allowBackup="false"`
- Monitoring deliberately strips prompt/response/title content
- Report vulnerabilities per [SECURITY.md](SECURITY.md)

## Known limitations (honest)

| Area | State |
|------|--------|
| Billing | **Mocked** (`USE_MOCK_BILLING=true`). Play products not configured |
| Crash reporting | **No-op** backend until a Sentry/Crashlytics DSN is configured |
| Nearby Share | **No-op** default; Play Services skeleton is fail-closed |
| Model downloads | Catalogue includes public Hugging Face URLs for development; production R2 not configured |
| Share relay | Web API exists in-repo; **not deployed** |
| Native inference | llama.cpp sources must be vendored manually; full device UI not verified in CI |
| Detekt | Runs in CI with `ignoreFailures=true` (alpha style debt deferred via baseline path) |

Details: [NOTES.md](NOTES.md).

## Release

See [RELEASE.md](RELEASE.md) and [CHANGELOG.md](CHANGELOG.md).  
Latest engineering alpha: **v0.1.0**.

## Roadmap

- [x] Engineering alpha foundation (docs, CI, LICENSE, modular core)
- [ ] Production R2 model hosting + catalogue API
- [ ] Play Billing products + server verification
- [ ] Deployed share relay
- [ ] Full Play Nearby on device
- [ ] Production crash backend
- [ ] Closed testing on Play

## License

**Business Source License 1.1 (BSL-1.1)** — full text in [LICENSE](LICENSE), summary in [LICENSING.md](LICENSING.md).

- **Copyright (c) 2026 David Ashe.** Original code and documentation ownership is retained by the author.
- Repository is published under the GitHub account **CoreLink-tech**.
- BSL-1.1 allows non-production use as stated in the license; a competing commercial product substantially similar to Hydra.AI requires a separate commercial license.
- Four years after publication of a given version, that version becomes available under Apache License 2.0.
- Commercial and data-collaboration inquiries: contact the repository owner via GitHub.

GitHub may display the license as “Other” because BSL is not always auto-classified like MIT/Apache; the binding text remains `LICENSE`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
