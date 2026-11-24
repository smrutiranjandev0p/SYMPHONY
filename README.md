# SYMPHONY

**Your music. In motion.**

SYMPHONY is a native Android music player built around a focused listening experience: **Flow · Home · Statistics · Library**. It brings music discovery, streaming and local playback, playlists, lyrics, audio controls and a customisable visual experience into one app.

<p align="left">
  <img alt="Version 1.1" src="https://img.shields.io/badge/version-1.1-9B8CFF?style=for-the-badge&labelColor=111827" />
  <img alt="Android" src="https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="License" src="https://img.shields.io/badge/license-see%20LICENSE-64748B?style=for-the-badge" />
</p>

---

## The app

| Area | Purpose |
| --- | --- |
| **Flow** | An immersive, swipe-led way to move through music recommendations |
| **Home** | Your listening starting point for discovery and quick access |
| **Statistics** | Listening activity and playback insights |
| **Library** | Your saved music, playlists, favourites, downloads and local collection |

The app also includes a full player and mini-player, playback queue, lyrics, search, service connections, widgets and a broad set of audio and appearance controls. Availability can vary by device, music source and connected service.

## Visual identity

SYMPHONY uses a dark, cinematic visual language with artwork-led surfaces, expressive motion and optional Liquid Glass styling.

**Glass Wave** is the refreshed signature app icon introduced with version 1.1: a glass-effect treble clef over a blue–violet–rose audio waveform, framed by a midnight-blue crystal surface.

The Appearance settings include selectable launcher icon editions:
- **Glass Wave** — refreshed signature identity
- **Desktop**
- **Pure White**
- **Obsidian Black**

Changing the selection updates the Android launcher icon through the app's existing launcher-alias manager.

## Highlights

- YouTube Music search, discovery and streaming integrations
- Local music library and offline downloads
- Playlists, favourites, queue and playback history
- Lyrics and synchronised lyrics where available
- Spotify playlist import and Last.fm scrobbling
- Equalizer, AutoEQ, Automix and advanced audio output options
- Google Cast, Android Auto, widgets and media controls
- Theme, accent, Liquid Glass and launcher icon customisation
- In-app release checking against this repository's GitHub Releases

Some features depend on device support, source availability, credentials or network connectivity.

## Technology

| Layer | Stack |
| --- | --- |
| App language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Playback | AndroidX Media3 / ExoPlayer |
| Async work | Kotlin Coroutines and Flow |
| Dependency injection | Hilt |
| Local persistence | Room, DataStore |
| Networking | OkHttp and Retrofit |
| Native audio | C++, CMake and the included USB audio driver |

## Build locally

### Requirements

- Android Studio
- JDK 17
- Android SDK 37
- Android 10 / API 29 or newer for the standard app configuration

### Clone

```bash
git clone https://github.com/smrutiranjandev0p/SYMPHONY.git
cd SYMPHONY
```

### Build a debug APK

**Windows PowerShell**
```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux**
```bash
./gradlew assembleDebug
```

The APK is generated under `app/build/outputs/apk/debug/`.

### Release builds

Release builds must be signed with the project's release keystore. Configure signing values through environment variables or local Gradle properties. Never commit keystores, passwords, API tokens or other credentials.

GitHub Actions contains the build and release workflows. Published releases and update checks are intended to use this repository only:

https://github.com/smrutiranjandev0p/SYMPHONY/releases

## Project structure

```text
SYMPHONY/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/symphony/app/
│       │   │   ├── data/       # Music sources, repositories and persistence
│       │   │   ├── playback/   # Playback service, queue and audio engine
│       │   │   ├── ui/         # Compose screens and components
│       │   │   └── ...
│       │   ├── res/            # Android resources and launcher icons
│       │   └── cpp/            # Native components
│       └── test/               # Unit and regression tests
├── audio/                      # Included audio driver module
├── .github/workflows/          # CI, APK builds and release automation
├── CHANGELOG.md
├── README.md
└── settings.gradle.kts
```

## Versioning

The refreshed release line starts at **1.1**. Android's internal `versionCode` remains monotonic for upgrade compatibility; the user-facing version is `versionName`.

See [CHANGELOG.md](CHANGELOG.md) for the version 1.1 starting point and subsequent changes.

## Project principles

- Keep the listening experience central.
- Keep navigation clear and consistent.
- Prefer reliable playback and honest source/quality reporting.
- Keep release signing material and private credentials out of Git.
- Verify builds before describing a change as shipped.

## License

See [LICENSE](LICENSE) for the project's license and distribution terms.

---

**SYMPHONY — Press play. Follow the flow.**
