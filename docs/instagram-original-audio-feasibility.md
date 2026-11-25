# Instagram Original Audio — Feasibility Research

**Branch:** `feature/instagram-audio-research`  
**Status:** Research only; no production playback changes.

## Goal

Evaluate whether SYMPHONY Flow can play the exact audio track carried by a shared Instagram Reel URL, without adding controls or changing the Flow UI.

## Current SYMPHONY architecture (inspected)

- `LinkPlaybackResolver` is the external-link entry point. It currently handles local audio, YouTube, Spotify, and generic search text.
- Spotify links are resolved to metadata through Spotify oEmbed, then matched to a YouTube Music track.
- `MusicPlayer` owns the app-wide Media3/ExoPlayer instance. This is the correct shared playback target for any future supported source.
- The current `StreamResolveCoordinator` / `PlaybackResolver` pipeline is keyed to YouTube video IDs and returns `YouTubeAudioStream`; it is not a generic arbitrary-URL resolver.
- Android manifest currently has VIEW filters for YouTube and Spotify, but not Instagram. Shared text URLs are handled through ACTION_SEND.

## Official Meta API findings

1. Instagram oEmbed is intended to render/embed public Instagram content. It returns embed HTML and basic embed metadata; it is not a raw audio-stream API. Tokenless oEmbed access is now documented, but that does not grant an audio URL.
2. The Instagram Media API requires an Instagram professional account and a user access token with the relevant permissions. Its `media_url` field can be omitted when a Reel contains copyrighted material or has been flagged for copyright violations.
3. The documented media fields include `media_audio_type` (for example, `MUSIC` or `ORIGINAL_SOUND`), but this is classification/metadata, not a playable audio stream.

## Feasibility conclusion

**Exact original Reel audio is not currently available through a documented, supported public API path for a third-party player.** A public Reel URL or oEmbed response cannot be treated as permission or a source URL for extracting the audio.

Even with a Meta developer app and a professional Instagram account, the documented Media API does not guarantee a downloadable/playable audio URL. Therefore, do not promise exact-original playback based on public metadata alone.

## Safe prototype scope

A future isolated prototype may:

- Recognize Instagram Reel/Post URLs shared to SYMPHONY.
- Normalize and validate the public URL.
- Obtain only metadata that Meta explicitly makes available for embedding or authorized media management.
- Display/retain the canonical Instagram URL and open the official Instagram embed/page when playback is not supported.
- Define a source-neutral stream contract for **user-owned or explicitly licensed audio URLs** and hand those URLs to the existing Media3 player, without routing them through the YouTube-only resolver.

It must not:

- Scrape private endpoints or reverse-engineer undocumented audio delivery APIs.
- Collect Instagram passwords, session cookies, or app tokens from the device.
- Bypass login, privacy, DRM, geographic, or copyright restrictions.
- Extract or persist media/audio content from oEmbed responses; Meta's oEmbed terms limit its use to front-end embedding.

## Next engineering decision

Before writing playback code, obtain one test asset that the user owns or is explicitly licensed to stream, plus its direct HTTPS audio URL and any required authorization/header contract. Then implement and test the generic Media3 handoff on this branch only. Instagram URL recognition can be added separately, but it must report “audio unavailable” rather than silently substitute a YouTube match when no authorized original stream exists.

## Source references

- Meta, [Instagram oEmbed](https://developers.facebook.com/docs/graph-api/reference/instagram-oembed/)
- Meta, [Instagram Media API](https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/)
- Meta, [oEmbed Video updates and tokenless access](https://developers.facebook.com/docs/graph-api/reference/oembed-video/)
