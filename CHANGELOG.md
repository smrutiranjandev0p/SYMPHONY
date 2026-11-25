# SYMPHONY Changelog

All notable user-facing and project changes are documented here. Release assets and platform-specific installation notes are published on the [GitHub Releases page](https://github.com/smrutiranjandev0p/SYMPHONY/releases).

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
