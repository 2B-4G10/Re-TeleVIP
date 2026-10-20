#!/usr/bin/env bash
# Prints the path of an apkeep binary (https://github.com/EFForg/apkeep), downloading it once.
set -euo pipefail
version=1.1.0
bin="${RUNNER_TEMP:-/tmp}/apkeep-$version"
if [ ! -x "$bin" ]; then
  curl -fsSL --retry 3 --retry-all-errors -o "$bin.part" \
      "https://github.com/EFForg/apkeep/releases/download/$version/apkeep-x86_64-unknown-linux-gnu"
  chmod +x "$bin.part" && mv "$bin.part" "$bin"
fi
echo "$bin"
