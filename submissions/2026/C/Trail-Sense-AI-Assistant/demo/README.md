# Demo Assets

This folder contains the demo assets for the Gemma 4 Hackathon submission.

Demo video:

- `trail-sense-ai-assistant-demo.mp4`

Release APK:

- `trail-sense-ai-assistant-release-unsigned.apk`

Raw links:

- Demo video: https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-demo.mp4
- Release APK: https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-release-unsigned.apk

SHA256:

- Demo video: `016255515b17f18a6e9a8f9a6c7b13c0b8dc66a1641e8b8062c935fdc05a29a6`
- Release APK: `c9495f5df919f878fa6a4acd9002d1519245be3dd6a30ec1b170452938db1069`

The APK was built with:

```bash
./gradlew :app:assembleRelease
```

Note: the repository's `release` build type does not configure a signing key, so the generated release APK is unsigned. Sign it before installing on a device, or use a signed debug/staging build for local recording.
