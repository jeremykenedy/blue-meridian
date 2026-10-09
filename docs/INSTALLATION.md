# Installation, update and removal

## Guided install or update

Enable ADB debugging on the TV, connect it to the computer, and run:

```bash
python3 install.py --serial TV_IP:5555
```

The installer shows the selected action and asks before changing the device. It retrieves the latest GitHub release metadata, downloads only this repository's APK and checksum, validates the published SHA-256, then asks ADB to install or update the package. It uses a private temporary directory and removes the downloaded file after installation. Use `--yes` to confirm install/update in scripts. Pass `--serial` whenever multiple devices are connected.

The installer contacts GitHub only after you run it. It does not send device identifiers, settings or usage data. The Android app itself has no Internet permission and performs no network requests.

## Install a local build

```bash
bash build.sh
python3 install.py --serial TV_IP:5555 --apk build/blue-meridian.apk
```

## Activate

Open the TV's screensaver or ambient display settings and select Blue Meridian. Menu names vary by Android version and device. The installer does not change the selected screensaver, sleep timers, display brightness or system update policy.

## Remove

Interactive removal prompts before uninstalling the package and its local settings:

```bash
python3 install.py --serial TV_IP:5555 --uninstall
```

Non-interactive removal requires all three explicit flags:

```bash
python3 install.py --serial TV_IP:5555 --uninstall --yes --force
```

Removing Blue Meridian does not change the device's selected screensaver or any system timers. If the TV still points to this app after removal, choose another screensaver in system settings.
