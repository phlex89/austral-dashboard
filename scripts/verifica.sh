#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

controlli=0

if [ -f package.json ]; then
  controlli=$((controlli + 1))
  npm run --if-present lint
  npm run --if-present test
fi

if [ -f Cargo.toml ]; then
  controlli=$((controlli + 1))
  cargo test
fi

if [ -f go.mod ]; then
  controlli=$((controlli + 1))
  go test ./...
fi

if [ -f Makefile ] && grep -qE '^test:' Makefile; then
  controlli=$((controlli + 1))
  make test
fi

if [ "$controlli" -eq 0 ]; then
  echo "Nessun controllo configurato: aggiungili in scripts/verifica.sh"
fi
