# Contributing to Hydra.AI

Thank you for your interest. This project is in early development.
Copyright (c) 2026 David Ashe. Contributions are subject to the project LICENSE (BSL-1.1).

## How to contribute

1. Fork the repository (or work on a branch if you have write access).
2. Create a feature branch from `main`.
3. Make focused, well-tested changes.
4. Ensure unit tests pass: `./gradlew testDebugUnitTest` (after `bash scripts/vendor-llama.sh` if needed).
5. Open a pull request against `main` with a clear description of the change and why it is needed.

## Coding standards

- Kotlin official style (`kotlin.code.style=official`).
- Prefer pure functions and domain types in `:core:model` when possible.
- Keep feature modules free of direct Android framework dependencies beyond Compose and ViewModel where practical.
- Never commit secrets, keystores, `.env.local`, or service-account JSON.
- Do not log user prompts or message content.

## Pull request checklist

- [ ] Unit tests added or updated for new logic
- [ ] No new hard-coded secrets or production URLs
- [ ] README / NOTES updated if behavior or setup changed
- [ ] `bash scripts/vendor-llama.sh` still works if native code was touched

## Questions

Open a GitHub issue with the `question` label or contact the repository owner.
