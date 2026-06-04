# Demo Assets

Place the recorded demo video in this folder.

Current APK:

- `trail-sense-ai-assistant-release-unsigned.apk`

This APK was built with:

```bash
./gradlew :app:assembleRelease
```

Note: the repository's `release` build type does not configure a signing key, so the generated release APK is unsigned. Sign it before installing on a device, or use a signed debug/staging build for local recording.
