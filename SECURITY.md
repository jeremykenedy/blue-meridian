# Security reporting

Please report suspected security vulnerabilities through GitHub's private vulnerability reporting feature for this repository. Do not publish exploit details in an issue or discussion. Include the affected release, device and Android version, and steps to reproduce when available.

The app has no runtime network permission, analytics, crash reporting or third-party runtime libraries. The standalone installer makes a user-requested HTTPS request to GitHub to download a release and verify its checksum.
