# Architecture – Hydra.AI

## High-level view

```
┌─────────────────────────────────────────────────────────────┐
│                         :app                                │
│  MainActivity · AppRouter · HydraApp (composition root)     │
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

## Core principles

1. **Privacy first** – prompts never leave the device for inference; shares are end-to-end encrypted.
2. **Offline capability** – once a model is installed, chat works without network.
3. **Entitlement isolation** – downloaded models keep working after a subscription expires.
4. **Pure domain where possible** – `:core:model` and many policy/planner classes are pure JVM.

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

## Dependency direction

- `app` → `feature` + `core`
- `feature` → `core` only
- `core` modules may depend on `:core:model` and selected other core modules; no upward dependency on `feature` or `app`
- `web` is independent (separate Node project)

## Future evolution

- Introduce Hilt (or similar) for the composition root
- Server-backed model catalogue with offline snapshot
- Nearby Connections implementation behind the existing transport interface
