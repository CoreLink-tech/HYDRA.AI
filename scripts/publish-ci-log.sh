#!/usr/bin/env bash
# Pushes a short build summary to the ci-logs branch so a failed build can be read without opening
# the Actions UI. Usage: publish-ci-log.sh <local-file> <name-on-branch>
# Needs GH_TOKEN and GITHUB_REPOSITORY (both set in GitHub Actions). Never fails the build.
set -uo pipefail

SRC="$1"
NAME="$2"
WORK="$(mktemp -d)"
REMOTE="https://x-access-token:${GH_TOKEN}@github.com/${GITHUB_REPOSITORY}.git"

for attempt in 1 2 3; do
  rm -rf "$WORK/logs"
  if git clone --depth 1 --branch ci-logs "$REMOTE" "$WORK/logs" 2>/dev/null; then :; else
    git init -q "$WORK/logs"
    git -C "$WORK/logs" checkout -q -b ci-logs
    git -C "$WORK/logs" remote add origin "$REMOTE"
  fi
  cp "$SRC" "$WORK/logs/$NAME"
  git -C "$WORK/logs" config user.name "hydra-ci"
  git -C "$WORK/logs" config user.email "ci@users.noreply.github.com"
  git -C "$WORK/logs" add -A
  git -C "$WORK/logs" commit -q -m "ci log: $NAME" || exit 0
  git -C "$WORK/logs" push -q origin ci-logs && exit 0
  sleep $((attempt * 3))
done
exit 0
