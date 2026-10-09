# Continuous integration

GitHub Actions runs on pushes to `main` and pull requests. The required checks build the app with Android SDK 36, run Java and installer tests, enforce 100 percent line and branch coverage for testable settings and installer logic, check Java formatting, validate documentation links, scan the manifest and Java source for runtime network/reporting code, and inspect the built APK permissions.

CI creates an unsigned review APK only. Release signing remains on the release workstation. Third-party actions use full commit SHAs. The workflows do not depend on SonarCloud or other token-gated quality plans; no unavailable provider badges are included.
