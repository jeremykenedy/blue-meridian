<p align="center">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="art/banner-dark.svg">
        <source media="(prefers-color-scheme: light)" srcset="art/banner-light.svg">
        <img src="art/banner-light.svg" alt="Blue Meridian animated Earth screensaver. No ads, analytics or tracking." width="800">
    </picture>
</p>

<p align="center">A continuously animated view of Earth from space for Fire TV, Android TV and Google TV.</p>

<p align="center">
    <a href="https://github.com/jeremykenedy/blue-meridian/releases"><img src="https://img.shields.io/github/downloads/jeremykenedy/blue-meridian/total" alt="Release downloads"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/releases"><img src="https://img.shields.io/github/v/release/jeremykenedy/blue-meridian?label=latest%20release" alt="Latest release"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/actions/workflows/ci.yml"><img src="https://github.com/jeremykenedy/blue-meridian/actions/workflows/ci.yml/badge.svg" alt="Build and test"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/actions/workflows/style.yml"><img src="https://github.com/jeremykenedy/blue-meridian/actions/workflows/style.yml/badge.svg" alt="Code style"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/actions/workflows/docs.yml"><img src="https://github.com/jeremykenedy/blue-meridian/actions/workflows/docs.yml/badge.svg" alt="Documentation checks"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/actions/workflows/security.yml"><img src="https://github.com/jeremykenedy/blue-meridian/actions/workflows/security.yml/badge.svg" alt="Privacy and manifest checks"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache--2.0-blue.svg" alt="Apache License 2.0"></a>
    <a href="https://github.com/jeremykenedy"><img src="https://img.shields.io/github/followers/jeremykenedy?label=Follow&style=social" alt="Follow Jeremy Kenedy on GitHub"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian" title="Open the repository and click Star"><img src="https://img.shields.io/badge/Star-this%20repo-yellow?logo=github&style=social" alt="Star this repository"></a>
    <a href="https://github.com/jeremykenedy/blue-meridian/stargazers"><img src="https://img.shields.io/github/stars/jeremykenedy/blue-meridian?style=social" alt="Repository stars"></a>
    <a href="https://github.com/sponsors/jeremykenedy"><img src="https://img.shields.io/badge/Sponsor-jeremykenedy-EA4AAA?logo=githubsponsors&logoColor=white" alt="Sponsor on GitHub"></a>
</p>

## Contents

- [Privacy](#privacy)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Screenshots](#screenshots)
- [Building and testing](#building-and-testing)
- [Documentation](#documentation)
- [License](#license)

## Privacy

Blue Meridian contains no ads, analytics, tracking, telemetry, crash reporting or runtime network access. The Android manifest declares no Internet permission. Preferences stay on the device. The standalone installer contacts GitHub only after you ask it to download a release APK and checksum; it sends no TV, device or usage data.

## Features

- Continuously rotating, shaded Earth rendered with OpenGL ES 2 and an original surface texture.
- Slow, natural and fast rotation speeds.
- Still, gentle and sweeping camera movement.
- Sparse, natural and dense star fields.
- Optional atmosphere glow, procedural moving clouds and city lights on the night side.
- Independent Random choices and an option to randomize all settings on each screensaver start.
- D-pad friendly settings screen and an app-owned settings provider for host apps.
- Standalone release installer, updater and uninstaller.
- No text or watermark in the screensaver scene.

## Requirements

- Fire TV, Android TV or Google TV with Android 6.0 or newer and OpenGL ES 2.0.
- ADB for the command-line installer.
- Android SDK Platform 36, Build Tools 36.0.0, Java and Python 3 to build from source.

The current automated device check uses an Android TV emulator running Android 12 at 1920x1080. No physical Fire TV or Google TV test is claimed in this release.

## Installation

Download the APK from [the latest release](https://github.com/jeremykenedy/blue-meridian/releases/latest) and install it with ADB, or use the included guided installer. Connect the TV with ADB debugging enabled, then run:

```bash
python3 install.py --serial TV_IP:5555
```

The installer downloads the latest stable release and verifies its SHA-256 checksum before installing or updating. To install a local build, pass `--apk build/blue-meridian.apk`. Select Blue Meridian in the device's screensaver or ambient display settings; the installer does not change device settings.

To remove it interactively:

```bash
python3 install.py --serial TV_IP:5555 --uninstall
```

For script use, `--uninstall --yes --force` explicitly confirms removal. See [installation and recovery](docs/INSTALLATION.md) for manual install, update and uninstall steps.

## Configuration

Open Blue Meridian from the TV launcher and use the remote to choose settings. Changes are applied when the screensaver starts again.

| Setting | Choices | Default |
| --- | --- | --- |
| Star density | Sparse, Natural, Dense, Random | Natural |
| Planet rotation | Slow, Natural, Fast, Random | Natural |
| Camera movement | Still, Gentle, Sweeping, Random | Gentle |
| Atmosphere glow | On, Off, Random | On |
| Night lights | On, Off, Random | On |
| Cloud cover | On, Off, Random | On |
| Randomize all settings at each start | On, Off | Off |

Host apps can read the provider schema and current values from `content://com.jeremykenedy.bluemeridian.settings/schema` and `content://com.jeremykenedy.bluemeridian.settings/settings`. See [configuration](docs/CONFIGURATION.md) for the schema, keys and update contract.

## Screenshots

Captures below come from the running Android TV emulator at 1920x1080. They show the animated app preview and the remote-operated settings screen.

<p align="center">
    <img src="art/screenshots/earth-preview.png" alt="Animated Earth from space in Blue Meridian" width="49%">
    <img src="art/screenshots/settings-emulator.png" alt="Blue Meridian settings displayed on Android TV emulator" width="49%">
</p>

## Building and testing

```bash
bash build.sh
bash test.sh
bash scripts/test-python-coverage.sh
bash scripts/test-coverage.sh
bash scripts/check-style.sh
python3 scripts/check-docs.py
python3 scripts/check-privacy.py
```

The coverage gates require 100 percent line and branch coverage for the settings resolver, validation rules and installer logic. Renderer behavior is verified with the emulator and recorded separately. CI builds an unsigned review APK; the signed release artifact is built and checked locally.

## Documentation

- [Architecture and rendering](docs/ARCHITECTURE.md)
- [Building from source](docs/BUILDING.md)
- [CI checks](docs/CI.md)
- [Configuration and provider schema](docs/CONFIGURATION.md)
- [Installation, update and removal](docs/INSTALLATION.md)
- [Release process](docs/RELEASING.md)
- [Test strategy](docs/TESTING.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Device verification](docs/VERIFICATION.md)
- [Version 1.0.0 notes](docs/releases/v1.0.0.md)
- [Security reporting](SECURITY.md)

Show some love by starring this repository on GitHub: [Star Blue Meridian](https://github.com/jeremykenedy/blue-meridian/stargazers).

## License

Blue Meridian is available under the [Apache License, Version 2.0](LICENSE). See [NOTICE](NOTICE) for project notices.
