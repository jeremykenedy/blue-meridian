# Release process

1. Update the version name and monotonically increasing version code in the release build environment.
2. Run the full local suite and build the signed APK with the saved Blue Meridian signing key.
3. Inspect package identity, manifest metadata, permissions, APK signature and SHA-256.
4. Confirm device testing, README, docs, screenshots and GitHub workflows describe the exact release candidate.
5. Push the candidate and wait for all required GitHub Actions to pass. Do not edit the candidate after the final green CI run; if an edit is needed, rerun the checks.
6. Run a read-only production sweep, create a SemVer tag and release notes, and attach `blue-meridian.apk` plus `blue-meridian.apk.sha256`.
7. Download the published assets and verify the checksum again.

Do not replace assets under an existing tag. Use a new patch version for corrections. The command syntax and upgrade path are documented in each release note.
