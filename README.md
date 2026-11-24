<!--
  SYMPHONY · README
  Keep this page aligned with shipped functionality and the current app version.
-->

<div align="center">

  <h1>SYMPHONY</h1>
  <p><strong>YOUR MUSIC. IN MOTION.</strong></p>
  <p><em>A more expressive way to discover, collect and experience music.</em></p>

  <p>
    <a href="https://github.com/smrutiranjandev0p/SYMPHONY">
      <img alt="GitHub repository" src="https://img.shields.io/badge/GitHub-SYMPHONY-111111?style=for-the-badge&logo=github&logoColor=white" />
    </a>
    <img alt="Version 5.2.1" src="https://img.shields.io/badge/version-5.2.1-CCFF00?style=for-the-badge&labelColor=111111" />
    <img alt="Platform Android" src="https://img.shields.io/badge/platform-Android-111111?style=for-the-badge&logo=android&logoColor=CCFF00" />
    <img alt="Built with Kotlin" src="https://img.shields.io/badge/Kotlin-Compose-111111?style=for-the-badge&logo=kotlin&logoColor=CCFF00" />
  </p>

  <p>
    <a href="#experience-the-flow">Experience</a> ·
    <a href="#the-symphony-experience">Features</a> ·
    <a href="#technology-stack">Technology</a> ·
    <a href="#get-started">Get started</a> ·
    <a href="#project-map">Project map</a>
  </p>

</div>

---

## The feeling behind SYMPHONY

Music deserves more than a list of tracks.

**SYMPHONY** is a native Android music experience designed around discovery, artwork and uninterrupted listening. It brings a rich local-and-streaming library together with an expressive player, deep audio controls and an immersive vertical discovery experience called **Flow**.

Think less navigating through menus. More getting lost in the next song.

<div align="center">

  **DISCOVER  /  FEEL  /  REPEAT**

  <sub>A colourful, artwork-first music player built around the next song—not the next menu.</sub>

</div>

## Experience the Flow

Flow is SYMPHONY's immersive, full-screen music stream. Every screen is a complete track moment: artwork takes centre stage, essential controls stay within one-hand reach, and a vertical swipe moves through recommendations.

```text
       SWIPE
         ↓
   ┌─────────────┐
   │   NEXT      │
   │   TRACK     │
   │             │
   │   ARTWORK   │
   │             │
   │  NOW PLAYING│
   └─────────────┘
         ↓
   QUEUE UPDATES
         ↓
     MUSIC PLAYS
```

Flow uses the app's playback queue, keeping playback, the mini-player and the full player in sync. The Home screen remains available whenever you want to browse instead of drift.

## The SYMPHONY experience

<table>
  <tr>
    <td width="50%">
      <h3>◉ Flow</h3>
      <p>Full-screen vertical discovery. Move through recommendations with a swipe and let the next track take over.</p>
    </td>
    <td width="50%">
      <h3>⌂ Home</h3>
      <p>Your listening home for quick picks, charts, new finds and recently played music.</p>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>▤ Library</h3>
      <p>Keep playlists, favourites, downloads and music stored on your device close at hand.</p>
    </td>
    <td width="50%">
      <h3>▥ Statistics</h3>
      <p>Your listening history, trends and playback insights in a dedicated space.</p>
    </td>
  </tr>
</table>

### Made for the way you listen

- **Discover & search** — YouTube Music browsing and streaming.
- **Your collection** — Local music, offline downloads, playlists and favourites.
- **Playback control** — Full player, mini-player, queue, shuffle and repeat.
- **Lyrics** — Lyrics, including synchronised lyrics where available.
- **Listening history** — Recently played tracks, history and sleep timer.
- **Connect your services** — Spotify import and Last.fm scrobbling.
- **Listen anywhere** — Google Cast, Android Auto, home-screen widgets and lock-screen controls.
- **Shape your sound** — Parametric EQ, AutoEQ, Automix and USB DAC audio capabilities.

> Feature availability can depend on the device, connected service, source and current implementation.

## A design language with a pulse

SYMPHONY is intentionally its own thing: dark, cinematic surfaces, vivid album art, translucent controls and motion that makes music feel present.

The interface is built with **Jetpack Compose** and **Material 3**, with an artwork-first, responsive and one-hand-friendly approach. The goal is simple: keep the music in focus and the controls close.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Language & UI | Kotlin, Jetpack Compose, Material 3 |
| Playback | AndroidX Media3 / ExoPlayer |
| Architecture & dependency injection | Hilt, Coroutines |
| Local data | Room, DataStore |
| Networking | Retrofit, OkHttp |
| Native audio | C++, Oboe, CMake |
| Music extraction | InnerTubeX with NewPipe Extractor fallback |

## Get started

### Requirements

- Android Studio
- JDK 17
- Android SDK 37
- Android 10 (API 29) or newer device / emulator

### Clone the repository

```bash
git clone https://github.com/smrutiranjandev0p/SYMPHONY.git
cd SYMPHONY
```

### Build a debug APK

**Windows (PowerShell):**

```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux:**

```bash
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/
```

> [!IMPORTANT]
> Release builds require the project's release-signing configuration. Keep signing keys, passwords, API tokens and other secrets out of source control. Use local properties or environment variables for private build configuration.

## Project map

```text
SYMPHONY/
├── app/
│   └── src/main/
│       ├── java/com/symphony/app/
│       │   ├── ui/           # Compose screens, Flow, player and navigation
│       │   ├── playback/     # Player service, queue and audio controls
│       │   ├── data/         # Database, preferences and music sources
│       │   ├── widget/       # Home-screen widgets
│       │   └── diagnostics/  # Logs and crash diagnostics
│       └── cpp/              # Native audio / bridge code
├── audio/
│   └── decent-usb-audio-driver/
├── assets/                   # SYMPHONY visual identity
├── docs/                     # Project notes and technical documentation
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Project status

SYMPHONY is actively evolving. The current project description identifies the Flow home experience, playback, lyrics, favourites, history, queue foundations, sleep timer and lock-screen metadata as implemented.

Ongoing work includes persistent playlist improvements, richer artist and album pages, broader DSP work and device-level background-playback verification.

## Contributing

Contributions, thoughtful feedback and bug reports are welcome.

Before making changes, please read:

- [Contribution guidelines](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Design guidelines](DESIGN.md)
- [Security policy](SECURITY.md)
- [Changelog](CHANGELOG.md)

Keep the experience fast, expressive and focused on the music.

## License

See the [LICENSE](LICENSE) file for the license and distribution terms.

---

<div align="center">

  **SYMPHONY** · **PRESS PLAY. FOLLOW THE FLOW.**

  <sub>Made for the moments between the first note and the last.</sub>

  [Explore the repository ↗](https://github.com/smrutiranjandev0p/SYMPHONY)

</div>
