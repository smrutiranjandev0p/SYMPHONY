# SYMPHONY Changelog

This changelog records the refreshed SYMPHONY release line, beginning with version **1.1** on 2026-11-25. Earlier version history is intentionally not carried into this sequence.

## [1.1] - 2026-11-25

### Added
- **Glass Wave identity:** A glass-effect treble-clef mark layered over a blue, violet and rose audio waveform, with a midnight-blue glass icon treatment.
- **App icon selector refresh:** A redesigned Appearance icon picker with visual previews, selection state and updated icon descriptions.
- **Repository-owned update flow:** The in-app update checker and release fallback point to the SYMPHONY GitHub repository.

### Changed
- **Fresh version line:** User-facing version name is 1.1; Android's internal version code is 34 to preserve monotonic upgrade compatibility.
- **Release workflow defaults:** Manual release tag and title defaults use 1.1.
- **Appearance identity:** The default launcher choice is named Glass Wave.
- **Settings integration:** The refreshed icon picker uses the existing icon preference and launcher-alias switching logic.
- **Project documentation:** README and release documentation describe the SYMPHONY project and its current release identity.

### Fixed
- **Settings integration:** Updated the icon selector call site to match the component signature.
- **Updater fallback:** Removed the previous external release-page fallback in favour of this repository's Releases page.

### Release verification
- **Published:** GitHub Release `1.1` is live (not a draft).
- **CI:** Release workflow run [#45482588828](https://github.com/smrutiranjandev0p/SYMPHONY/actions/runs/45482588828) completed successfully, including Android APK builds, signing verification and release publication.
- **Assets:** Universal APK, uncompressed universal APK, Android 7 legacy APK, vivo Origin edition APK and SHA-256 checksum file were attached to the release.
- **Download:** [SYMPHONY 1.1 releases](https://github.com/smrutiranjandev0p/SYMPHONY/releases/tag/1.1).

> CI success confirms the automated build, signing checks and publication. Installation and in-place upgrade behaviour still need validation on physical Android devices.
