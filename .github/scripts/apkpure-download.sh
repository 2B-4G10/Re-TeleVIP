#!/usr/bin/env bash
# Downloads apkpure:<package>@<version> (see client-builds.sh) to the given file, as APK or XAPK.
#
#   apkpure-download.sh apkpure:org.telegram.plus@13.0.2.0 client.apk
#
# APKPure has been caught serving altered apps (USAGE-apkpure.md in apkeep), which is why apkeep
# asks for acknowledge_dangers. Client watch only reads the code of what it downloads and never
# installs it, so that does not matter here.
set -euo pipefail
spec=${1#apkpure:} out=$2
apkeep=$(bash "$(dirname "$0")/apkeep.sh")
work=$(mktemp -d)
"$apkeep" -a "$spec" -d apk-pure -o 'acknowledge_dangers=true,arch=arm64-v8a' "$work" >&2
file=$(find "$work" -type f \( -name '*.apk' -o -name '*.xapk' \) | head -n1)
[ -n "$file" ] || { echo "apkeep downloaded nothing for $spec." >&2; rm -rf "$work"; exit 1; }
mv "$file" "$out"
rm -rf "$work"
