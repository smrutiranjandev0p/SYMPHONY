# SYMPHONY

### Your music, in motion.

**SYMPHONY** is a native Android music experience for discovering, collecting and listening without losing the feeling of the music. It pairs a rich local-and-streaming library with an artwork-first interface, deep audio controls and an immersive vertical **Flow**.

> A colourful, expressive music player built around the next song—not the next menu.

## The experience

| Space | What it does |
| --- | --- |
| **Flow** | A full-screen, vertical music stream. Swipe up or down to move through recommendations; each settled card starts its track. |
| **Dashboard** | A familiar home for quick picks, charts, new finds and recently played music. |
| **Library** | Keep playlists, favourites, downloads and local device music close. |
| **Player** | Large artwork, queue-aware playback, lyrics and the controls that matter. |
| **Listen deeper** | Equaliser, AutoEQ, lossless options, sleep timer, listening history and more. |

## Flow: music that moves with you

Flow is SYMPHONY's front door. Each screen is a complete track moment: artwork fills the display, essential controls remain within one-hand reach and a vertical swipe advances to the next recommendation. It uses the app's existing playback queue, so playback, the mini-player and the full player stay in sync.

```text
Swipe up / down  →  settle on a track  →  queue updates  →  music starts
```

The original Dashboard is still available from Flow whenever you want to browse instead of drift.

## Built for serious listening

- YouTube Music browsing and streaming
- Local music, offline downloads and playlist support
- Full player, mini-player, queue, shuffle and repeat
- Lyrics, including synchronised lyrics where available
- Favourites, history, sleep timer and search
- Spotify import, Last.fm scrobbling and listening statistics
- Google Cast, Android Auto, home-screen widgets and lock-screen controls
- Parametric EQ, AutoEQ, Automix and USB DAC audio capabilities

## Design language

SYMPHONY is intentionally its own thing: dark, cinematic surfaces; vivid album art; translucent controls; and motion that makes music feel present. The UI is built with Jetpack Compose and Material 3, following an artwork-first, responsive, one-hand-friendly approach.

## Technology

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Media3 / ExoPlayer** for playback
- **Hilt**, **Room**, **DataStore**, **Coroutines** and **Retrofit**
- Native C++/Oboe audio pipeline for advanced playback features

## Getting started

### Requirements

- Android Studio with JDK 17
- Android SDK 37
- An Android device or emulator running Android 10 (API 29) or newer

### Build

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is produced under `app\build\outputs\apk\debug\`.

## Project structure

```text
app/src/main/java/com/symphony/app/
├── ui/          # Compose screens, Flow, player and navigation
├── playback/    # Player service, queue, sessions and audio controls
├── data/        # Local database, preferences and music sources
├── widget/      # Home-screen widgets
├── diagnostics/ # Logs and crash diagnostics
└── ...
audio/           # Native USB audio driver module
```

## Status

SYMPHONY is actively evolving. The Flow home experience, playback, lyrics, favourites, history, queue foundations, sleep timer and lock-screen metadata are in place. Persistent playlist improvements, richer artist/album pages, broader DSP work and device-level background-playback verification continue next.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) and the repository's [AGENTS.md](AGENTS.md) before making changes. Keep the experience fast, expressive and focused on the music.

---

**SYMPHONY** — press play, then follow the flow.