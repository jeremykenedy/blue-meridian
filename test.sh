#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/build/tests"
mkdir -p "$OUT"
javac -source 8 -target 8 -d "$OUT" \
  "$ROOT/src/com/jeremykenedy/bluemeridian/BlueMeridianOptions.java" \
  "$ROOT/src/com/jeremykenedy/bluemeridian/SettingsValues.java" \
  "$ROOT/tests/BlueMeridianOptionsTest.java"
java -ea -cp "$OUT" com.jeremykenedy.bluemeridian.BlueMeridianOptionsTest
python3 -m unittest -v tests.test_installer
