#!/usr/bin/env bash
# Fresh-clone web verification without cloud accounts.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/web"
if [[ ! -f .env.local ]]; then
  cp .env.example .env.local
  echo "Created web/.env.local from example (USE_MOCK_PLAY / file DB recommended)."
fi
npm ci
npm test
npm run typecheck
echo "web verify OK"
