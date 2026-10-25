#!/usr/bin/env bash
# Prints the path of an apkeep binary (https://github.com/EFForg/apkeep), downloading it once.
set -euo pipefail
version=1.1.0
# SHA-256 of that release's x86_64 Linux binary: a changed download is refused, not run.
sha256=badd7ad9fa7d2f32abe01eaf33b7ff086361a95bbc000a33bf44b630d4bf2140
bin="${RUNNER_TEMP:-/tmp}/apkeep-$version"
if [ ! -x "$bin" ]; then
  curl -fsSL --retry 3 --retry-all-errors -o "$bin.part" \
      "https://github.com/EFForg/apkeep/releases/download/$version/apkeep-x86_64-unknown-linux-gnu"
  if ! echo "$sha256  $bin.part" | sha256sum -c --quiet -; then
    rm -f "$bin.part"
    echo "::error::apkeep $version does not match its pinned SHA-256; not running it." >&2
    exit 1
  fi
  chmod +x "$bin.part" && mv "$bin.part" "$bin"
fi
echo "$bin"
