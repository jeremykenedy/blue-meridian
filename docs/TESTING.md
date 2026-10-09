# Test strategy

`bash test.sh` runs deterministic tests for setting resolution, random ranges, invalid-value fallbacks, provider validation and installer behavior. Installer tests cover safe release URLs, trusted redirects, response size limits, checksum parsing and mismatch rejection, temporary-file permissions and cleanup, multiple-device selection, confirmation and install/update/uninstall commands.

`bash scripts/test-python-coverage.sh` enforces 100 percent line and branch coverage for `install.py`. `bash scripts/test-coverage.sh` enforces 100 percent line and branch coverage for the pure Java settings resolver and validator. Framework classes, Android lifecycle and OpenGL driver behavior are not represented as host-code coverage; verify those on an Android TV emulator as described in [device verification](VERIFICATION.md).

For an emulator check, install the APK using an explicit serial, open Settings, navigate with D-pad and Select, change each option, launch Preview animation, wait for the scene to settle and capture the output. Query the settings provider to confirm persisted values. Check Android logs for shader compilation, GL errors and service crashes.
