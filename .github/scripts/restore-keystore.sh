#!/usr/bin/env bash
# Recreates release.jks and keystore.properties from the KEYSTORE_* secrets.
set -euo pipefail

# Tolerate how the secret tends to get pasted: CRLF line endings, wrapped lines, surrounding
# quotes, and certutil-style "-----BEGIN/END-----" lines.
if ! printf '%s\n' "$KEYSTORE_BASE64" | grep -v -- '-----' | tr -d " \t\r\n\"'" \
        | base64 --decode > release.jks 2>/dev/null; then
    echo "::error::KEYSTORE_BASE64 is not base64. Paste the output of: base64 -i televip-release.jks (macOS) or [Convert]::ToBase64String([IO.File]::ReadAllBytes(\"televip-release.jks\")) (PowerShell)."
    exit 1
fi

if ! keytool -list -keystore release.jks -storepass "$KEYSTORE_PASSWORD" -alias "$KEY_ALIAS" >/dev/null 2>&1; then
    echo "::error::The KEYSTORE_* secrets do not open a keystore containing alias '$KEY_ALIAS'. KEYSTORE_BASE64 must be the base64 of the .jks file, with the matching KEYSTORE_PASSWORD and KEY_ALIAS."
    exit 1
fi

{
    echo "storeFile=release.jks"
    echo "storePassword=$KEYSTORE_PASSWORD"
    echo "keyAlias=$KEY_ALIAS"
    echo "keyPassword=$KEY_PASSWORD"
} > keystore.properties
