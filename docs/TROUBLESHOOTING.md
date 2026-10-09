# Troubleshooting

## Installer cannot find the TV

Confirm ADB debugging is enabled, approve the computer on the TV and run `adb devices -l`. Use `--serial` to select one connected device. The installer does not attempt to configure a network or pair with a TV.

## APK update is rejected

Android only updates a package signed by the same signing key. Keep the Blue Meridian release keystore and password together in protected storage. Do not uninstall an existing build before troubleshooting; uninstalling removes its local settings.

## Blue Meridian does not start when idle

Select it in system screensaver or ambient display settings and confirm the device's idle timeout. Settings activity's Preview animation tests rendering independently of automatic activation.

## Scene is black or incomplete

Use a device that supports OpenGL ES 2.0. Restart the preview, check `adb logcat` for OpenGL shader or texture errors, and verify that the app package is installed without Android having rejected the resource. The bundled emulator verification uses Android TV API 31 at 1920x1080; other vendors and resolutions have not all been tested.

## Remove the app

Run `python3 install.py --serial TV_IP:5555 --uninstall` and confirm the prompt. The installer does not alter the system's current screensaver selection or timer values.
