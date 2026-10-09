# Device verification

## Android TV emulator

The renderer and settings were tested on an Android TV emulator with model `sdk_google_atv64_arm64`, Android 12 (API 31), and 1920x1080 logical output. The APK was installed with ADB, the native settings activity opened, D-pad input changed the star density, the settings schema and updated values were queried through the exported provider, and the OpenGL preview was captured after startup. The Android service query lists Blue Meridian as a DreamService. The emulator has no screensaver settings activity, so system-triggered dream activation could not be exercised. This verifies the preview and settings on that emulator only; it does not establish physical Fire TV, physical Google TV, automatic idle activation on other vendors, native 4K composition or sustained thermal behavior.

The screenshots in `../art/screenshots/` are direct 1920x1080 emulator captures from October 8, 2026. The scene capture came from the running preview after eight seconds of startup; the settings capture came directly from the settings activity. Both were taken with `adb -s emulator-5562 exec-out screencap -p`. The in-app dream preview is resized from the same scene capture to 384x216. The scene contains no text or overlays. The emulator image has no resolvable `android.settings.DREAM_SETTINGS` activity, so system-triggered DreamService activation and exit were not verified there. The secure screensaver component and enabled value remained unchanged.

## Checks still required for broader device claims

| Device | Status | Follow-up |
| --- | --- | --- |
| Android TV emulator, API 31, 1920x1080 | Preview and settings tested. System-triggered dream activation unavailable in this emulator. | Verify activation on a device with a screensaver settings activity. |
| Physical Fire TV | Untested; no physical device was available for this release. | If you have a Fire TV, test selection, idle activation, remote exit, sleep/wake persistence and extended playback, then report the model, Fire OS version and results. |
| Physical Android TV | Untested; no physical device was available for this release. | If you have an Android TV, test service discovery, selection, idle activation, remote exit and extended playback, then report the model, Android version and results. |
| Physical Google TV | Untested; no physical device was available for this release. | If you have a Google TV, test service discovery, selection, idle activation, remote exit and extended playback, then report the model, Android version and results. |
| Native 4K and sustained display behavior | Untested on a 4K display. | Test native 4K composition, frame pacing, thermal behavior and idle power on supported hardware, then report the device model, display mode and observations. |
