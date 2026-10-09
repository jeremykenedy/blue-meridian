# Building from source

## Requirements

- Java 21 or newer
- Android SDK Platform 36 and Build Tools 36.0.0
- Python 3
- `adb` for device installation
- `cwebp` only when replacing the bundled texture asset

## Build a signed APK

```bash
bash build.sh
```

The build creates `build/blue-meridian.apk` and its SHA-256 file. The first local build creates a unique signing key under `~/.android/blue-meridian.jks` and stores its password in `~/.android/blue-meridian.pass`. Keep both files private and backed up. Future builds must use the same key to update the existing app. Override the paths with `BLUE_MERIDIAN_KEYSTORE` and `BLUE_MERIDIAN_KEYPASS` when needed.

Set `VERSION_NAME` and `VERSION_CODE` before building a new release. CI builds an unsigned APK and never receives the local signing key.

## Run on a TV emulator

```bash
adb devices -l
adb -s TV_SERIAL install -r build/blue-meridian.apk
adb -s TV_SERIAL shell am start -n com.jeremykenedy.bluemeridian/.SettingsActivity
```

For the full scene preview, select Preview animation in the settings screen. Always pass the intended ADB serial when more than one device is connected.
