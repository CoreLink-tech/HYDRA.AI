# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/).

## [0.1.0-alpha] – 2026-11-05

First public engineering alpha of Hydra.AI. Core modules and feature shells are in place; production hosting, Play products, and the share relay are not yet deployed.

### Added
- On-device LLM chat architecture (Kotlin, Jetpack Compose, llama.cpp JNI)
- Modular Gradle project: `app`, `core/*`, `feature/*`, `web/`
- SQLCipher-encrypted local database with Android Keystore-wrapped passphrase
- Resumable model downloads with SHA-256 integrity checks (WorkManager)
- End-to-end encrypted share path (AES-256-GCM; key in URL fragment only)
- Free vs Pro entitlement policy (offline chat never gated)
- Mock billing gateway for development; Play Billing client scaffold
- Unit tests for routing, crypto, integrity, entitlement, planners, catalogue
- Instrumented smoke test scaffold
- Next.js relay / auth / billing verification / admin scaffold under `web/`
- Business Source License 1.1
- SECURITY.md, CONTRIBUTING.md, ARCHITECTURE.md, CI workflow
- Repository description and topics

### Known limitations
- `USE_MOCK_BILLING=true` by default; Play products not configured
- Model catalogue still points at public Hugging Face URLs for development
- Share relay and R2 model hosting not deployed
- Full UI not verified on physical devices in CI
- Native llama.cpp sources must be vendored via `scripts/vendor-llama.sh`

## [Unreleased]

### Added (Phase 4)
- `AppContainer` composition-root interface (Hilt-ready boundary)
- `NoOpNearbyShareTransport` + `PlayNearbyShareTransport` skeleton
- Consent-gated crash reporter wired in `HydraApp` (noop backend until DSN configured)
- `ModelCatalogSource` / `LocalModelCatalogSource` for future network catalogue refresh
- Detekt static analysis (`config/detekt/detekt.yml`) + CI job
- Compose UI tests for offline banner copy (`AppRouterComposeTest`)

### Planned
- Production R2 model hosting and catalogue API
- Play Console products and server-side verification
- Deployed share relay
- Full Play Nearby ConnectionsClient implementation on device
- Signed release builds via `keystore.properties`
- Optional full Hilt migration of ViewModel factories

[0.1.0-alpha]: https://github.com/CoreLink-tech/HYDRA.AI/releases/tag/v0.1.0-alpha
