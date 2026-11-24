# Contributing to SYMPHONY

Thank you for your interest in SYMPHONY. Bug reports, thoughtful feedback, documentation improvements and code contributions are welcome.

## Before you contribute

Please read:

- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Design guidelines](DESIGN.md) for UI and interaction changes
- [Project instructions](AGENTS.md) for repository-specific development rules
- [Security policy](SECURITY.md) before reporting a vulnerability

## Getting started

### Prerequisites

- Android Studio (current stable version recommended)
- JDK 17
- Android SDK Platform 37
- Android SDK Build-Tools and the NDK/CMake versions required by the project
- Git

The app's default minimum Android version is Android 10 (API 29). The project also has a legacy build configuration; check the current Gradle configuration before changing supported Android versions.

### Clone this repository

```bash
git clone https://github.com/smrutiranjandev0p/SYMPHONY.git
cd SYMPHONY
```

### Build a debug APK

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

macOS / Linux:

```bash
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

### Run tests

```bash
./gradlew testDebugUnitTest
```

Use the Gradle wrapper committed with the repository. Do not commit generated APKs, signing keys, passwords, API tokens, local environment files or other secrets.

## Development workflow

- The `main` branch is the primary development branch.
- Create a focused branch for each change, for example:
  - `feat/flow-lyrics-toggle`
  - `fix/player-like-action`
  - `docs/update-contributing-guide`
- Keep pull requests small and focused.
- Follow Kotlin conventions and the existing Compose, ViewModel, repository and playback patterns.
- For UI changes, follow [DESIGN.md](DESIGN.md) and check loading, empty, error and long-content states.
- Avoid unrelated formatting changes and do not introduce third-party support links or services that are not part of SYMPHONY.

## Opening an issue or pull request

For bugs, include:

1. SYMPHONY version and Android version/device.
2. What you expected to happen.
3. What actually happened.
4. Reproduction steps and relevant logs or screenshots (remove personal data and secrets).

For pull requests, include:

1. A concise description of the change and its motivation.
2. The relevant issue, if one exists.
3. Tests/build tasks run and their results.
4. Screenshots or a short recording for UI changes.

Open issues and pull requests in this repository:

https://github.com/smrutiranjandev0p/SYMPHONY/issues

Please be respectful, constructive and mindful that SYMPHONY is maintained as an independent project.
