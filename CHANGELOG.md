# SYMPHONY Changelog

All project changes from the refreshed release line are recorded here. The new public version sequence starts at **1.1** on 2026-11-25. Earlier version history is intentionally not carried into this fresh changelog.

## [1.1] - 2026-11-25

### Added
- **Glass Wave identity:** Introduced a new glass-effect treble-clef mark layered over a blue, violet and rose audio waveform, with a midnight-blue glass icon treatment.
- **App icon selector refresh:** Rebuilt the Appearance app-icon picker with larger visual previews, rounded selection surfaces, a clear selected-state indicator and updated icon descriptions.
- **Repository-owned update flow:** Aligned the in-app update checker and release-page fallback with the SYMPHONY GitHub repository.

### Changed
- **Fresh version line:** Reset the user-facing version name to 1.1 and advanced Android's internal version code to 34 so upgrades remain monotonic.
- **Release workflow defaults:** Updated the manual release tag and release title defaults to 1.1.
- **Appearance identity copy:** Renamed the default launcher choice to Glass Wave and updated its description to match the new artwork.
- **Settings integration:** Connected the refreshed icon picker to the existing app-icon preference and launcher-alias switching logic.

### Fixed
- **Settings source structure:** Corrected the App Icon selector call-site parameters to match the new component signature.
- **Updater fallback references:** Removed the old external release-page fallback from the update manager in favour of this project's Releases page.

### Verification
- GitHub Actions build workflows were triggered for the updated source. A successful build and a published 1.1 release are required before this version can be considered ready to install.
