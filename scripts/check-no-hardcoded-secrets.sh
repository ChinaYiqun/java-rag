#!/usr/bin/env bash
set -euo pipefail

# This guard checks the current source tree only. It is intentionally not a
# substitute for rotating credentials that existed in Git history.
PATTERN='(sk-[A-Za-z0-9_-]{16,}|jina_[A-Za-z0-9_-]{16,}|(api[_-]?key|password|passwd|secret)[[:space:]]*=[[:space:]]*"[^"$]{8,}")'

if grep -RInE --exclude-dir=target --exclude='*.md' "$PATTERN" src/main/java; then
  echo "Hard-coded credential-like value detected in src/main/java." >&2
  echo "Use environment variables or JVM system properties instead." >&2
  exit 1
fi

echo "No hard-coded credential-like values found in the current Java source tree."
