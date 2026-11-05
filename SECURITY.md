# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| main    | :white_check_mark: |
| < 0.1   | :x:                |

This project is pre-release. Security fixes are applied on the `main` branch.

## Reporting a Vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities.

1. Email the repository owner (see the GitHub profile or commit author email) with a clear description.
2. Include steps to reproduce, affected components, and (if possible) a suggested fix.
3. Allow reasonable time for a response before any public disclosure.

We will acknowledge receipt and work with you on a coordinated disclosure timeline.

## Security Design Highlights

- Local database uses SQLCipher with a passphrase wrapped by an Android Keystore AES-GCM key.
- Conversation sharing uses AES-256-GCM; the key never leaves the device except inside the URL fragment.
- Cleartext HTTP traffic is disabled (`network_security_config.xml`).
- `android:allowBackup="false"`.
- Crash / monitoring path is designed never to transmit user prompts or message content.
- No production secrets, API keys, or private keys are committed to this repository.

## License and disclosure

This project is licensed under Business Source License 1.1 (see LICENSE). Security reports are welcome regardless of license questions.

## Scope

In-scope: Android app modules, native llama.cpp integration, `web/` API and share relay.

Out-of-scope (for now): third-party model weights hosted on Hugging Face, Play Console configuration, production deployment infrastructure that is not present in this repository.
