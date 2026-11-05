# Architecture – Hydra.AI

## High-level view

```
┌─────────────────────────────────────────────────────────────┐
│                         :app                                │
│  MainActivity · AppRouter · HydraApp (composition root)     │
│  (database, repos, llama, downloads, billing gateway)       │
└────────────┬───────────────────────────────┬────────────────┘
             │                               │
   ┌─────────▼─────────┐           ┌─────────▼─────────┐
   │   :feature/*      │           │   :core/*         │
   │ onboarding chat   │  uses     │ model database    │
   │ models settings   │ ────────► │ llama download    │
   │ (Compose + VM)    │           │ billing sharing   │
   └───────────────────┘           │ device monitoring │
                                   │ designsystem      │
                                   └─────────┬─────────┘
                                             │
                                   ┌─────────▼─────────┐
                                   │      web/         │
                                   │ Next.js relay +   │
                                   │ auth + billing    │
                                   └───────────────────┘
```

## Composition root

`HydraApp` implements `AppContainer`, an explicit interface over long-lived dependencies. Feature ViewModels already take constructor parameters; a future Hilt migration maps each `AppContainer` property to a `@Singleton` binding without redesigning feature modules.


`HydraApp` owns long-lived infrastructure:

| Dependency | Type | Notes |
|------------|------|-------|
| `database` | `HydraDatabase` | SQLCipher; passphrase from `KeyProvider` (Android Keystore) |
| `chats` / `models` / `settings` | repositories | Room DAOs |
| `engine` | `LlamaEngine` | JNI wrapper; load/generate/unload |
| `downloads` | `ModelDownloads` | WorkManager + OkHttp Range + SHA-256 |
| `billing` | `BillingGateway` | `MockBillingGateway` while `USE_MOCK_BILLING=true` |

Feature ViewModels receive these through factories in `MainActivity`. No global service locator outside `HydraApp`.

## Core principles

1. **Privacy first** – prompts never leave the device for inference; shares are end-to-end encrypted.
2. **Offline capability** – once a model is installed, chat works without network.
3. **Entitlement isolation** – downloaded models keep working after a subscription expires.
4. **Pure domain where possible** – `:core:model` and many policy/planner classes are pure JVM and unit-tested without the Android runtime.

## Module responsibilities

### `:core:model`
Pure Kotlin domain: `Entitlement`, `ModelDescriptor`, `ModelCatalog`, `ChatMessage`, tiers, plans. No Android imports.

### `:core:database`
Room entities/DAOs, SQLCipher open helper, `KeyProvider` (Keystore-wrapped AES-GCM passphrase), repositories.

### `:core:llama`
JNI bridge to llama.cpp, `LlamaEngine` (mutex-guarded load/generate), ChatML prompt builder, streaming `Flow<GenerationEvent>`.

### `:core:download`
`HttpModelFetcher` (resumable), `Integrity` (SHA-256), `ResumePlan`, `ModelDownloadWorker` (WorkManager), `DownloadBus` events.

### `:core:billing`
`BillingGateway` interface, `MockBillingGateway`, `PlayBillingGateway`, `EntitlementPolicy`, product ids.

### `:core:sharing`
`ShareCrypto` (AES-256-GCM), `ShareLink` (fragment key), `ShareService`, `HttpShareRelay`, Nearby transport interface.

### `:core:device` / `:core:monitoring` / `:core:designsystem`
Device tier recommendation, opt-in crash reporting (no prompts), Material 3 theme.

### `:feature:*`
Compose UI + ViewModels. Depend only on `core`. Pure planners (`SettingsPlanner`, `OnboardingFlow`, `ModelsPlanner`, `ReplyAssembler`) are JVM-testable.

### `:app`
Navigation shell, onboarding gate, bottom bar, offline banner, composition root wiring.

### `web/`
Independent Next.js 16 app: marketing, admin, auth sessions (hashed bearer tokens), share relay (ciphertext only), Play purchase verification, model catalogue API.

## Data flow (chat)

1. User sends message → `ChatViewModel`
2. History loaded from `ChatRepository` (SQLCipher)
3. Prompt built by `LlamaEngine` (ChatML)
4. Tokens streamed via JNI → UI + throttled persistence
5. On stop / end → final message status written to DB

## Data flow (share)

1. `ShareService` serializes payload → AES-256-GCM seal on device
2. Ciphertext + IV uploaded to relay (Bearer auth)
3. App builds link: `https://<site>/s/<id>#k=<base64url-key>`
4. Relay stores only ciphertext; cannot decrypt

## Data flow (billing)

1. UI calls `BillingGateway.purchase(productId)`
2. Mock path grants local Pro for testing; Play path returns a purchase token
3. Token is sent to `web /api/v1/billing/google-play/verify`
4. Server is source of truth; app caches entitlement for UI only
5. Offline chat never consults entitlement

## Dependency direction

- `app` → `feature` + `core`
- `feature` → `core` only
- `core` modules may depend on `:core:model` and selected other core modules; no upward dependency on `feature` or `app`
- `web` is independent (separate Node project)

## Security boundaries

| Boundary | Mechanism |
|----------|-----------|
| Local DB at rest | SQLCipher + Keystore-wrapped passphrase |
| Network | `cleartextTrafficPermitted=false` |
| Backups | `allowBackup="false"` |
| Shares | AES-GCM; key only in URL fragment |
| Monitoring | Prompt fields stripped before any report |
| Secrets | Never committed; see `web/.env.example`, `keystore.properties.example` |

## Build variants

| Field | Debug default | Release intent |
|-------|---------------|----------------|
| `USE_MOCK_BILLING` | `true` | `false` once Play products exist |
| `RELAY_BASE_URL` | `""` (offline) | Production relay origin |
| Minify / shrink | off | on |

## Future evolution

- Optional full Hilt migration of ViewModel factories (boundary already exists via `AppContainer`)
- Network-backed `ModelCatalogSource` using `GET /api/v1/models`
- Complete Play Nearby ConnectionsClient on device (`PlayNearbyShareTransport` is the hook)
- Real `PlayBillingGateway` Activity provider when mock billing is disabled
- Production `CrashReporter` backend (Sentry/Crashlytics) behind existing consent + Scrubber
