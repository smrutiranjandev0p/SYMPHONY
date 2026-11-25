<p align="center">
  <img src="assets/SYMPHONY_banner_v2_1440p.png" alt="SYMPHONY — Your music. In motion." width="100%" />
</p>

<h1 align="center">SYMPHONY</h1>
<p align="center"><strong>Your music. In motion.</strong><br/>
A modern Android listening experience built around Flow, discovery and control.</p>

<p align="center">
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/smrutiranjandev0p/SYMPHONY?style=for-the-badge&color=9B8CFF" /></a>
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/smrutiranjandev0p/SYMPHONY?style=for-the-badge&color=F5C451" /></a>
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/network/members"><img alt="GitHub forks" src="https://img.shields.io/github/forks/smrutiranjandev0p/SYMPHONY?style=for-the-badge&color=64748B" /></a>
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/smrutiranjandev0p/SYMPHONY?style=for-the-badge&color=38BDF8" /></a>
</p>

<p align="center">
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/releases/latest"><strong>⬇ Download APK</strong></a>
  &nbsp;·&nbsp;
  <a href="#features"><strong>Explore features</strong></a>
  &nbsp;·&nbsp;
  <a href="#community"><strong>Contribute</strong></a>
</p>

SYMPHONY is a native Android music player built around a focused listening experience: **Flow · Home · Statistics · Library**. It brings music discovery, streaming and local playback, playlists, lyrics, audio controls and a customisable visual experience into one app.

<p align="left">
  <img alt="Latest release" src="https://img.shields.io/github/v/release/smrutiranjandev0p/SYMPHONY?display_name=tag&style=for-the-badge&label=latest&color=9B8CFF" />
  <img alt="Android" src="https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="License" src="https://img.shields.io/badge/license-see%20LICENSE-64748B?style=for-the-badge" />
</p>

## Download SYMPHONY

Get the newest published build from GitHub Releases. The release page is the source of truth for available APK variants and compatibility notes.

<p align="center">
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/releases/latest">
    <img alt="Download latest SYMPHONY release" src="https://img.shields.io/badge/⬇%20DOWNLOAD-LATEST%20RELEASE-9B8CFF?style=for-the-badge&labelColor=111827" />
  </a>
</p>

Choose the APK that matches your Android device from the **Assets** section of the latest GitHub release:

| Your device / requirement | APK to choose |
| --- | --- |
| **Most Android phones** | `SYMPHONY-vX-universal.apk` — recommended compressed universal APK; check the release notes for minimum Android version |
| Older phones (Android 7+) | `SYMPHONY-vX-android7.apk` — legacy-compatible build |
| Need the uncompressed package | `SYMPHONY-vX-universal-uncompressed.apk` |
| Supported Vivo OriginOS edition | `SYMPHONY-vX-vivo-origin.apk` — only if listed in that release's assets |

**Not sure?** Choose the standard **universal APK** for most modern Android phones. Check the release notes and minimum Android version before installing. If GitHub shows multiple APK variants, select the one matching your device and requirements.

<p align="center">
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/releases/latest">View all APK options & release notes</a>
  &nbsp;·&nbsp;
  <a href="https://github.com/smrutiranjandev0p/SYMPHONY/releases">All versions</a>
</p>

Downloads are hosted on GitHub Releases. Verify the downloaded APK against `SHA256SUMS.txt` when the checksum file is provided.

---

## The app

| Area | Purpose |
| --- | --- |
| **Flow** | An immersive, swipe-led way to move through music recommendations |
| **Home** | Your listening starting point for discovery and quick access |
| **Statistics** | Listening activity and playback insights |
| **Library** | Your saved music, playlists, favourites, downloads and local collection |

Flow uses an artwork-first layout with a centred, direct play/pause control, larger inset-aware search/settings actions, floating heart particles and no duplicate global mini-player overlay while the Flow tab is active. It also offers an optional **Party Torch** mode: after explicit camera and microphone permission, the phone flash reacts to detected audio peaks while music plays; pulses are rate-limited and the torch turns off when playback pauses or Flow is left. The app also includes a full player and a mini-player on the other tabs, playback queue, lyrics, search, service connections, widgets and a broad set of audio and appearance controls. Availability can vary by device, music source and connected service.

## Visual identity

SYMPHONY uses a dark, cinematic visual language with artwork-led surfaces, expressive motion and optional Liquid Glass styling.

**Glass Wave** is the default signature app icon for the 1.2 release line: a glass-effect treble clef over a blue–violet–rose audio waveform, framed by a midnight-blue crystal surface.

The adaptive launcher icon now points to the Glass Wave foreground for both standard and round launcher masks. The Appearance settings also include selectable launcher icon editions:
- **Glass Wave** — refreshed signature identity
- **Desktop**
- **Pure White**
- **Obsidian Black**

Changing the selection updates the Android launcher icon through the app's existing launcher-alias manager.

## Features

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

GitHub Actions contains the build and release workflows. Published releases and update checks use this repository. For the latest APK and device-specific variants, use the [Download SYMPHONY](https://github.com/smrutiranjandev0p/SYMPHONY/releases/latest) section above.

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

The release page shows the latest published version and its downloadable assets. Android `versionCode` is kept monotonic for upgrade compatibility; `versionName` is the user-facing version. See [CHANGELOG.md](CHANGELOG.md) for release-by-release changes.

## Project principles

- Keep the listening experience central.
- Keep navigation clear and consistent.
- Prefer reliable playback and honest source/quality reporting.
- Keep release signing material and private credentials out of Git.
- Verify builds before describing a change as shipped.

## Community

SYMPHONY is an independent project maintained by **[smrutiranjandev0p](https://github.com/smrutiranjandev0p)**.

- ⭐ **Like the project?** Star the repository to show support and help others discover it.
- 🐛 **Found a bug?** [Open an issue](https://github.com/smrutiranjandev0p/SYMPHONY/issues/new/choose) with your device model, Android version and steps to reproduce.
- 💡 **Have an idea?** Start a [discussion or feature request](https://github.com/smrutiranjandev0p/SYMPHONY/issues).
- 🤝 **Want to contribute?** Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

Please star only if you genuinely find the project useful. No star exchanges or artificial engagement.

## Credits & inspiration

SYMPHONY's Flow screen takes inspiration from the immersive, swipe-led music discovery interaction popularised by Resso. SYMPHONY is an independent project and is not affiliated with or endorsed by Resso, Vivo, YouTube, Spotify or other referenced services.

## Maintainer

**Made with care by [Smruti Ranjan Behera](https://github.com/smrutiranjandev0p).**

## License

See [LICENSE](LICENSE) for the project's license and distribution terms.

---

<p align="center"><strong>SYMPHONY — Press play. Follow the flow.</strong><br/>If it adds something to your listening experience, consider leaving a ⭐.</p>
