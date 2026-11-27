# SYMPHONY Changelog

## [2.1] - 2026-11-27

### Added
- **Flow Atmosphere:** Added seven independently selectable Flow visual effects: Heartfall, Audio Reactive Aura, Cinematic Motion, Stardust, Aurora Flow, Light Sweep and Dreamy Bokeh.
- **Atmosphere controls:** Added intensity, particle density and audio-sensitivity controls with persistent preferences.
- **Premium artwork borders:** Added No Border, Titanium, Crystal, Aurora, Obsidian and Prism styles, with the original slim border restored as the baseline treatment.
- **Documentation index:** Organized project documentation into design, architecture and development sections.

### Changed
- **Flow lifecycle:** Atmosphere effects are scoped to the visible Flow screen and pause when Flow is not in the foreground.
- **Audio reactivity:** Audio Reactive Aura uses the existing playback analyser; it does not request microphone permission.
- **Version metadata:** Set `versionName` to `2.1` and Android `versionCode` to `41`.

### Validation
- Merged Flow Atmosphere/artwork-border work and repository documentation organization into `main`.
- CI release build, APK signature verification and release publication are being run for this version. Physical-device behaviour should be evaluated on the target device.

---

## [2.0] - 2026-11-26

<https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/2.0>

### Added
- **Purple Glass launcher identity:** Added the supplied deep-violet glass adaptive launcher icon with the SYMPHONY treble-clef and waveform mark.
- **Monochrome launcher artwork:** Added matching monochrome artwork for supported themed-icon launchers.
- **Launcher editions:** Restored dedicated Desktop, Pure White and Obsidian Black launcher artwork resources.

### Changed
- **Appearance icon selector:** Uses the supplied Purple Glass artwork in the selector and selected-icon header; Desktop, Pure White and Obsidian Black previews each reference their own artwork.
- **Adaptive launcher resources:** Updated adaptive icon foreground/background assets while retaining the existing launcher-alias switching architecture and selectable icon editions.
- **Playback stack:** Aligned AndroidX Media3 ExoPlayer, OkHttp datasource, HLS and DASH modules on 1.9.0, with Jellyfin FFmpeg decoder 1.9.0+1; updated custom audio sink implementations and player callbacks for the Media3 API.
- **Flow live recommendations:** Flow's vertical pager observes the active live playback queue while Flow owns playback, exposing radio-continuation tracks appended in the background to manual swiping. Existing recommendation ordering, deduplication, radio continuation and stream resolution remain intact.
- **Stream resolution:** Retained the InnerTubeX-first stream resolution path and NewPipe fallback architecture.
- **Version metadata:** Set `versionName` to `2.0` and Android `versionCode` to `40`.

### Validation and release
- **Published:** [SYMPHONY 2.0](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/2.0).
- **Release workflow:** [#45819793575](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45819793575) completed successfully.
- The workflow built the Universal, Universal Uncompressed, vivo Origin Island and Android 7 legacy APK variants, verified APK signatures, generated SHA-256 checksums and published the GitHub Release.
- **Android compatibility:** Universal and uncompressed universal builds target Android 10+; the legacy build targets Android 7.0–9.0. The vivo Origin Island build is a separate package for supported vivo OriginOS configurations.
- Android 7–11 devices may use a translucent acrylic fallback instead of platform-dependent live-blur effects.
- CI verifies the automated build and publication steps. Physical-device installation, in-place upgrade, OEM launcher icon switching and device-specific playback/audio behaviour still require testing on the relevant hardware.

### Release assets
- `SYMPHONY-v2.0-universal.apk` — recommended Android 10+ package.
- `SYMPHONY-v2.0-universal-uncompressed.apk` — uncompressed Android 10+ package.
- `SYMPHONY-v2.0-vivo-origin.apk` — separate vivo Origin Island edition.
- `SYMPHONY-v2.0-android7.apk` — Android 7.0–9.0 legacy package.
- `SHA256SUMS.txt` — SHA-256 checksums for release assets.

---
 
## [1.5] - 2026-11-25

### Fixed
- **Home → Flow playback reflection:** Flow now places the currently playing YouTube Music track at the front of its visible track collection so the Flow artwork and track details can reflect playback started from Home.
- **Settings → Flow return reset:** Flow ignores the initial restored pager page as a user selection. Returning from Settings no longer automatically sends a play command for Flow's first recommendation and overwrites the active Home track.

### Changed
- **Shared playback source of truth:** Flow reads the app-wide MusicPlayer state for the active track and playback status rather than assuming its first recommendation is the active song.
- **Version metadata:** User-facing `versionName` is `1.5`; Android `versionCode` is `38`.
- **Release workflow:** Updated the release workflow trigger and manual defaults for tag `1.5`.
- **Documentation:** Updated README download/release references and project metadata for v1.5.

### Validation
- GitHub Actions build and debug workflows are running for the v1.5 source changes. Their results and release asset links will be added after completion.
- Automated CI does not prove physical-device behaviour. Home → Flow track reflection, Settings → Flow return behaviour, playback-position continuity and device-specific audio must still be checked on a real device.
- Flow's current catalogue is YouTube Music-track based; local-only tracks without a YouTube video ID are not represented by this specific synchronization change.

---

## [1.4] - 2026-11-25

### Added
- **InnerTubeX playback integration:** Added the InnerTubeX-based stream extraction path to the playback stack, with the existing NewPipe/local resolver path retained as a fallback.
- **About & System build details:** The About area displays the installed app version and Android build number.
- **Release showcase:** Added the current SYMPHONY v1.4 promotional banner to the repository README.

### Changed
- **Version metadata:** User-facing `versionName` is `1.4`; Android `versionCode` is `37`.
- **Launcher identity:** Refreshed the alternate launcher icon artwork around the Glass Wave treble-clef and waveform identity while preserving the individual Desktop, Pure White and Obsidian Black backgrounds.
- **Appearance selector:** Updated the icon previews so Glass Wave, Desktop, Pure White and Obsidian Black show their corresponding selectable artwork.
- **Documentation:** Reworked the README download guidance, feature overview, audio/streaming architecture, updater behaviour, development instructions and verification notes.

### Fixed
- **Icon consistency:** Aligned the Appearance selector previews with the artwork used by their corresponding launcher aliases.
- **README artwork path:** Corrected the hero image reference to the uploaded v1.4 banner asset.

### Release assets
- `SYMPHONY-v1.4-universal.apk` — recommended Android 10+ release.
- `SYMPHONY-v1.4-vivo-origin.apk` — separate vivo OriginOS-targeted package.
- `SYMPHONY-v1.4-android7.apk` — Android 7–9 legacy package.
- `SYMPHONY-v1.4-universal-uncompressed.apk` — uncompressed universal package.
- `SHA256SUMS.txt` — SHA-256 checksums for published assets.

### Validation
- **Android build workflow:** [#45563020342](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45563020342) completed successfully.
- **Release publication workflow:** [#45563020178](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45563020178) completed successfully.
- The release assets were published to [GitHub Release 1.4](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.4).
- Automated CI does not prove physical-device installation, in-place upgrade, OEM launcher refresh, vivo OriginOS integration or hardware-specific audio/torch behaviour. Those checks remain device-dependent.

---

## [1.3] - 2026-11-25

### Added
- **Party Torch Light Studio:** Long-pressing Party Torch opens a dedicated control sheet. Adjustable flash strength is exposed only when the device reports compatible hardware support; otherwise the app retains standard torch behaviour.
- **README showcase refresh:** Replaced the previous logo-led banner with a product showcase reflecting Flow, Appearance, Statistics and Library.

### Changed
- **Unified icon family:** Desktop, Pure White and Obsidian Black icon editions use the Glass Wave treble-clef and waveform artwork while retaining their distinct backgrounds.
- **Version metadata:** User-facing `versionName` is `1.3`; Android `versionCode` is `36`.
- **Documentation:** Updated download links, APK variant guidance, feature descriptions, visual customisation notes, project structure and release verification status.

### Validation
- **Android build:** GitHub Actions build workflow [#45545506080](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45545506080) completed successfully.
- **Release publication:** GitHub Actions release workflow [#45545506015](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45545506015) completed successfully.
- **Published:** [SYMPHONY 1.3](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.3) is live with universal, Android 7+, uncompressed universal and vivo Origin APK variants plus `SHA256SUMS.txt`.
- Physical-device installation, in-place upgrade and hardware-specific torch behaviour still require device testing.

---

## [1.2] - 2026-11-25

### Changed
- **Flow controls:** Removed the redundant dashboard shortcut and enlarged the search and settings targets with status-bar-aware top spacing.
- **Playback layout:** Moved the primary play/pause action into a prominent centred glass-style control; kept Like, More and Lyrics actions available below the track details.
- **Flow mini-player:** Suppressed the global bottom mini-player only while the Flow tab is active, avoiding duplicate playback controls without changing the other tabs.
- **Heart animation:** Increased the floating hearts' travel distance and animation duration.
- **Auto-advance sync:** Flow follows the current queue item when playback advances automatically, while manual swipes continue to select and play the chosen recommendation.
- **Party Torch:** Added an opt-in Flow control that uses the active playback audio session to detect waveform peaks and pulse the device flash. Camera and microphone permissions are requested only after enabling it; pulses are rate-limited and the torch is switched off when playback pauses or Flow is left.
- **Launcher identity:** Pointed both adaptive default launcher masks to the Glass Wave foreground artwork so the icon selector's default and installed launcher icon agree.
- **Version metadata:** Set user-facing `versionName` to `1.2` and Android `versionCode` to `35`.

### Validation
- The v1.2 source changes were included in the release line. Refer to the [1.2 release](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.2) for the published assets.

---

## [1.1] - 2026-11-25

### Added
- **Glass Wave identity:** A glass-effect treble-clef mark layered over a blue, violet and rose audio waveform, with a midnight-blue glass icon treatment.
- **App icon selector refresh:** A redesigned Appearance icon picker with visual previews, selection state and updated icon descriptions.
- **Repository-owned update flow:** The in-app update checker and release fallback point to the SYMPHONY GitHub repository.

### Changed
- **Fresh version line:** User-facing version name is `1.1`; Android's internal version code is `34` to preserve monotonic upgrade compatibility.
- **Release workflow defaults:** Manual release tag and title defaults use 1.1.
- **Appearance identity:** The default launcher choice is named Glass Wave.
- **Settings integration:** The refreshed icon picker uses the existing icon preference and launcher-alias switching logic.
- **Project documentation:** README and release documentation describe the SYMPHONY project and its current release identity.

### Fixed
- **Settings integration:** Updated the icon selector call site to match the component signature.
- **Updater fallback:** Removed the previous external release-page fallback in favour of this repository's Releases page.

### Release verification
- **Published:** GitHub Release [1.1](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.1) is live.
- **CI:** Release workflow run [#45482588828](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45482588828) completed successfully, including Android APK builds, signing verification and release publication.
- **Assets:** Universal APK, uncompressed universal APK, Android 7 legacy APK, vivo Origin edition APK and SHA-256 checksum file were attached to the release.
- Installation and in-place upgrade behaviour still need validation on physical Android devices.
