# SYMPHONY Changelog

All notable project changes are documented here. Release status and downloadable artifacts are linked to the official GitHub Releases page.

## [1.4] - 2026-11-25

### Fixed
- **Launcher icon selection:** Corrected the in-app icon previews so Glass Wave, Desktop, Pure White and Obsidian Black display the matching selectable artwork.
- **Launcher icon artwork:** Updated all three alternate launcher foregrounds to the current Glass Wave treble-clef and waveform identity, retaining each edition's distinct background.
- **Icon consistency:** The Appearance selector artwork and launcher-alias artwork now use the same visual identity for each option.

### Changed
- **Version metadata:** User-facing `versionName` is `1.4`; Android `versionCode` is `37`.
- **About & System:** The update row reports the installed app version and build number.

### Validation
- Release and Android build workflows are running for v1.4.
- Launcher switching still needs verification on a physical device and supported launcher; CI can validate compilation but cannot prove OEM launcher refresh behaviour.

---

## [1.3] - 2026-11-25

### Added
- **Party Torch Light Studio:** Long-pressing Party Torch opens a dedicated control sheet. Adjustable flash strength is exposed only when the device reports compatible hardware support; otherwise the app retains standard torch behaviour.
- **README showcase refresh:** Replaced the previous logo-led banner with a current product showcase reflecting Flow, Appearance, Statistics and Library.

### Changed
- **Unified icon family:** Desktop, Pure White and Obsidian Black icon editions use the current Glass Wave treble-clef and waveform artwork while retaining their distinct backgrounds.
- **Version metadata:** User-facing `versionName` is `1.3`; Android `versionCode` is `36`.
- **Documentation:** Updated download links, APK variant guidance, feature descriptions, visual customisation notes, project structure and release verification status.

### Validation
- **Android build:** GitHub Actions build workflow [#45545506080](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45545506080) completed successfully.
- **Release publication:** GitHub Actions release workflow [#45545506015](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45545506015) completed successfully.
- **Published:** [SYMPHONY 1.3](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.3) is live with universal, Android 7+, uncompressed universal and Vivo Origin APK variants plus `SHA256SUMS.txt`.
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
- **Assets:** Universal APK, uncompressed universal APK, Android 7 legacy APK, Vivo Origin edition APK and SHA-256 checksum file were attached to the release.
- Installation and in-place upgrade behaviour still need validation on physical Android devices.
