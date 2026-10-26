#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
TOOLS="$SDK/build-tools/36.0.0"
ANDROID_JAR="$SDK/platforms/android-36/android.jar"
OUT="$ROOT/build"
KEYSTORE="${BLUE_MERIDIAN_KEYSTORE:-$HOME/.android/blue-meridian.jks}"
KEYPASS="${BLUE_MERIDIAN_KEYPASS:-$HOME/.android/blue-meridian.pass}"
VERSION_NAME="${VERSION_NAME:-1.0.1}"
VERSION_CODE="${VERSION_CODE:-2}"
SIGN_APK="${SIGN_APK:-true}"

for tool in javac zip; do
  command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done
if [[ "$SIGN_APK" == "true" ]]; then
  for tool in keytool openssl; do
    command -v "$tool" >/dev/null || { echo "Missing required signing tool: $tool" >&2; exit 1; }
  done
elif [[ "$SIGN_APK" != "false" ]]; then
  echo "SIGN_APK must be true or false." >&2
  exit 1
fi
[[ -f "$ANDROID_JAR" && -x "$TOOLS/aapt2" ]] || {
  echo "Install Android SDK platform 36 and build-tools 36.0.0." >&2; exit 1;
}
mkdir -p "$OUT"
rm -rf "$OUT/classes" "$OUT/dex" "$OUT/generated"
mkdir -p "$OUT/classes" "$OUT/dex"

if [[ "$SIGN_APK" == "true" ]]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  if [[ ! -f "$KEYSTORE" ]]; then
    [[ ! -e "$KEYPASS" ]] || { echo "Signing key is missing but its password file exists; restore the key." >&2; exit 1; }
    (umask 077 && openssl rand -hex 24 > "$KEYPASS")
    keytool -genkeypair -keystore "$KEYSTORE" -storepass:file "$KEYPASS" -alias blue-meridian \
      -keyalg RSA -keysize 3072 -validity 10950 -dname "CN=Blue Meridian Release"
    chmod 600 "$KEYSTORE"
  fi
  [[ -s "$KEYPASS" ]] || { echo "Restore the signing-key password file." >&2; exit 1; }
fi

"$TOOLS/aapt2" compile --dir "$ROOT/res" -o "$OUT/res.zip"
"$TOOLS/aapt2" link -o "$OUT/unsigned.apk" -I "$ANDROID_JAR" \
  --manifest "$ROOT/AndroidManifest.xml" --min-sdk-version 23 --target-sdk-version 36 \
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
  --java "$OUT/generated" "$OUT/res.zip"
find "$ROOT/src" "$OUT/generated" -name '*.java' > "$OUT/sources.txt"
javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
  -d "$OUT/classes" @"$OUT/sources.txt"
find "$OUT/classes" -name '*.class' > "$OUT/classes.txt"
"$TOOLS/d8" --release --lib "$ANDROID_JAR" --min-api 23 --output "$OUT/dex" @"$OUT/classes.txt"
(cd "$OUT/dex" && zip -q -j "$OUT/unsigned.apk" classes.dex)
mkdir -p "$OUT/notices/assets"
cp "$ROOT/NOTICE" "$OUT/notices/assets/NOTICE"
cp "$ROOT/LICENSE" "$OUT/notices/assets/LICENSE"
cp "$ROOT/assets/SOURCES.json" "$OUT/notices/assets/SOURCES.json"
(cd "$OUT/notices" && zip -q "$OUT/unsigned.apk" assets/NOTICE assets/LICENSE assets/SOURCES.json)
"$TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
if [[ "$SIGN_APK" == "true" ]]; then
  "$TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-pass "file:$KEYPASS" --ks-key-alias blue-meridian \
    --out "$OUT/blue-meridian.apk" "$OUT/aligned.apk"
  "$TOOLS/apksigner" verify "$OUT/blue-meridian.apk"
else
  cp "$OUT/aligned.apk" "$OUT/blue-meridian.apk"
fi
(cd "$OUT" && shasum -a 256 blue-meridian.apk > blue-meridian.apk.sha256)
printf 'Built %s\n' "$OUT/blue-meridian.apk"
cat "$OUT/blue-meridian.apk.sha256"
