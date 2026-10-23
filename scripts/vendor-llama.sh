#!/usr/bin/env bash
# Fetches llama.cpp at a pinned tag into core/llama/third_party/llama.cpp.
# Sources only: no binaries are downloaded or committed (see .gitignore).
set -euo pipefail

LLAMA_TAG="${LLAMA_TAG:-b10232}"
LLAMA_REPO="${LLAMA_REPO:-https://github.com/ggml-org/llama.cpp}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/core/llama/third_party/llama.cpp"

if [ -f "$DEST/include/llama.h" ]; then
  echo "llama.cpp already present at $DEST (remove the folder to re-vendor)"
  exit 0
fi

command -v git >/dev/null || { echo "git is required"; exit 1; }
mkdir -p "$(dirname "$DEST")"
git clone --depth 1 --branch "$LLAMA_TAG" "$LLAMA_REPO" "$DEST"
echo "vendored llama.cpp $LLAMA_TAG -> $DEST"
