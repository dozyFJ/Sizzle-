#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK directory}"
: "${SIZZLE_KEYSTORE_PASSWORD:?Set the dedicated Sizzle keystore password}"
"$ANDROID_HOME/build-tools/35.0.0/zipalign" -f -p 4 app/build/outputs/apk/release/app-release-unsigned.apk app/build/outputs/apk/release/sizzle-aligned.apk
"$ANDROID_HOME/build-tools/35.0.0/apksigner" sign --ks .signing/sizzle-demo.jks --ks-key-alias sizzle-demo --ks-pass env:SIZZLE_KEYSTORE_PASSWORD --out app/build/outputs/apk/release/Sizzle-0.1.0.apk app/build/outputs/apk/release/sizzle-aligned.apk
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose --print-certs app/build/outputs/apk/release/Sizzle-0.1.0.apk
