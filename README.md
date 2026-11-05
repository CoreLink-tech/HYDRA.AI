# Hydra.AI

**Private, on-device AI chat for Android.**

Hydra.AI runs a small language model entirely on your phone via llama.cpp. After the model is downloaded once, chat works offline and nothing you type leaves the device. Optional conversation sharing uses end-to-end encryption: the key is generated on-device and travels only in the link fragment, so the relay stores ciphertext it cannot read.

> **Status:** Core modules are implemented and unit-tested. Feature modules and the app shell compile. Full device verification, production model hosting, Play Billing products, and the share relay deployment are still in progress. See [NOTES.md](NOTES.md).

## Problem & users

Most mobile AI chat products send every prompt to a remote server. Hydra.AI targets users who want useful local assistance (writing, study, light coding help) without giving a third party permanent access to their conversations. Primary audience: privacy-conscious Android users on mid-to-high-end devices (3 GB+ RAM).

## Key features

- Fully offline chat after one model download
- Curated, quantized GGUF models (Fast / Balanced / Advanced tiers)
- Device-aware model recommendation (RAM, storage, thermal, ABI)
- Resumable downloads with SHA-256 verification
- End-to-end encrypted conversation sharing (AES-256-GCM)
- Free vs Pro entitlement (offline chat is never gated by subscription)
- Encrypted local database (SQLCipher + Android Keystore)

## Architecture overview

Modular multi-module Gradle project approximating Clean Architecture:

- `:core:model` – pure domain types (no Android)
- Other `:core:*` modules – infrastructure (DB, download, llama, billing, sharing…)
- `:feature:*` – Compose screens + ViewModels
- `:app` – composition root, navigation, manual DI via `HydraApp`
- `web/` – Next.js relay, auth, billing verification, admin, marketing

See [ARCHITECTURE.md](ARCHITECTURE.md) (to be expanded) for diagrams and data-flow details.

## Module map

| Module | Purpose |
|--------|---------|
| `app` | Navigation, onboarding gate, bottom bar, `HydraApp` |
| `core/model` | Entitlement, ModelDescriptor, ChatMessage, catalogues |
| `core/database` | Room + SQLCipher + KeyProvider + repositories |
| `core/llama` | JNI + LlamaEngine (streaming generation) |
| `core/download` | OkHttp Range + WorkManager + Integrity |
| `core/billing` | BillingGateway, Mock, Play, EntitlementPolicy |
| `core/sharing` | ShareCrypto, ShareService, HttpShareRelay |
| `core/device` | DeviceChecker + TierRecommender |
| `core/monitoring` | Opt-in crash reporting (prompts never sent) |
| `core/designsystem` | Material 3 dark theme |
| `feature/*` | Onboarding, Chat, Models, Settings screens |
| `web` | Next.js 16 + Turso + Drizzle API |

## Technology stack

- Kotlin 2.0, AGP 8.7, Compose BOM 2024.12, Room 2.6, SQLCipher, OkHttp, WorkManager, Play Billing
- llama.cpp (vendored at tag b10232)
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
./gradlew :core:model:testDebugUnitTest \
          :core:billing:testDebugUnitTest \
          :core:sharing:testDebugUnitTest \
          :core:download:testDebugUnitTest \
          :core:device:testDebugUnitTest \
          :core:monitoring:testDebugUnitTest \
          :core:database:testDebugUnitTest \
          :feature:chat:testDebugUnitTest \
          :feature:onboarding:testDebugUnitTest \
          :feature:models:testDebugUnitTest \
          :feature:settings:testDebugUnitTest \
          :app:testDebugUnitTest

# Optional full debug APK (requires NDK + CMake)
./gradlew assembleDebug
```

Open the project root in Android Studio and let Gradle sync.

## Local development – Web

```bash
cd web
cp .env.example .env.local   # fill required vars – never commit real values
npm install
npm run db:migrate
npm run dev
```

## Environment variables

See [`web/.env.example`](web/.env.example). Never commit `.env.local`, keystores, or service-account JSON.

## Security notes

- Database passphrase is random, Keystore-wrapped (AES-GCM), never leaves the device.
- Share keys exist only in the URL fragment.
- Cleartext HTTP is disabled via network security config.
- `allowBackup="false"`.
- Monitoring deliberately strips prompt content.
- Report vulnerabilities via the process in [SECURITY.md](SECURITY.md).

## Build & test commands

| Goal | Command |
|------|---------|
| All unit tests | `./gradlew testDebugUnitTest` |
| Specific modules | see Local development above |
| Debug APK | `./gradlew assembleDebug` |
| Clean | `./gradlew clean` |
| Web typecheck | `cd web && npm run typecheck` |

## Release & changelog

- Versioning and signed-build steps: [RELEASE.md](RELEASE.md)
- Notable changes: [CHANGELOG.md](CHANGELOG.md)
- Optional signing template: [keystore.properties.example](keystore.properties.example)

## License

Copyright (c) 2026 CoreLink-tech / ZenLink Technologies.  
See [LICENSE](LICENSE) for terms (Business Source License 1.1).  
Commercial licensing and data-collaboration inquiries are welcome; contact the repository owner.

## Roadmap (high level)

- [x] Engineering alpha docs, LICENSE, CI, architecture (Phases 1–3 foundation)
- [ ] Production R2 model hosting + catalogue API
- [ ] Play Billing products + server verification
- [ ] Deployed share relay
- [ ] First public alpha on Play (closed testing)
- [ ] Nearby share transport
- [ ] Compose UI journey tests

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
