package com.symphony.app.ui.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.symphony.app.data.local.FlowArtworkStyle
import com.symphony.app.ui.settings.SettingsViewModel
import com.symphony.app.data.music.HomeShelfItem
import com.symphony.app.data.music.YouTubeMusicTrack
import com.symphony.app.playback.toPlayableTrack
import com.symphony.app.playback.toGeneratedTrack
import com.symphony.app.data.playlist.isLikedIn
import com.symphony.app.ui.player.LocalMusicPlayer
import com.symphony.app.ui.player.LocalOpenPlayerLyrics
import com.symphony.app.ui.player.PlayerViewModel
import com.symphony.app.ui.common.TrackContextMenuSheet
import com.symphony.app.ui.common.TrackMenuCapabilities
import com.symphony.app.ui.common.TrackMenuTarget
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow

/**
 * Artwork-first discovery home. It deliberately reuses FeedViewModel's live
 * recommendations and MusicPlayer queue, so swiping changes real playback
 * instead of maintaining a second catalogue or player state.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlowScreen(
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val musicPlayer = LocalMusicPlayer.current
    val openPlayerLyrics = LocalOpenPlayerLyrics.current
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appearanceTheme by settingsViewModel.theme.collectAsStateWithLifecycle()
    val artworkStyle = appearanceTheme?.flowArtworkStyle ?: FlowArtworkStyle.TITANIUM
    val likedKeys by playerViewModel.likedTrackKeys.collectAsStateWithLifecycle()
    var menuTrack by remember { mutableStateOf<YouTubeMusicTrack?>(null) }
    var partyTorchEnabled by remember { mutableStateOf(false) }
    val playback by musicPlayer.stateWithoutProgress.collectAsStateWithLifecycle()
    // The process-wide player is the source of truth across Home, Flow and
    // Settings. A Home/library track may not be part of the recommendation
    // feed, so pin the currently playing YouTube track into Flow's visual
    // catalogue instead of showing an unrelated first recommendation.
    val tracks = remember(state.feedData, playback.current, playback.queue, playback.sourceLabel) {
        // While Flow owns playback, its live queue is the catalogue—not the
        // finite Home feed snapshot. Radio continuation appends recommendations
        // to this queue in the background; observing it here makes those songs
        // available to manual vertical swipes as soon as they arrive.
        val liveFlowQueue = if (playback.sourceLabel == "Flow") {
            playback.queue.mapNotNull { queued ->
                queued.videoId?.takeIf(String::isNotBlank)?.let { videoId ->
                    YouTubeMusicTrack(
                        videoId = videoId,
                        title = queued.title,
                        artist = queued.artist,
                        album = queued.album,
                        artworkUrl = queued.artworkUrl,
                        durationSeconds = queued.durationMs?.div(1_000L)?.toInt(),
                    )
                }
            }.distinctBy(YouTubeMusicTrack::videoId)
        } else {
            emptyList()
        }
        if (liveFlowQueue.isNotEmpty()) {
            liveFlowQueue
        } else {
            buildList {
                addAll(state.feedData.quickPicks)
                addAll(state.feedData.charts)
                addAll(state.feedData.freshFinds)
                addAll(state.feedData.ytRecentSongs)
                addAll(state.feedData.ytLikedSongs)
                addAll(state.feedData.becauseYouListenTo?.items.orEmpty())
                addAll(
                    state.feedData.nativeShelves.flatMap { shelf ->
                        shelf.items.filterIsInstance<HomeShelfItem.Track>().map { it.track }
                    },
                )
                playback.current?.takeIf { !it.videoId.isNullOrBlank() }?.let { current ->
                    add(
                        0,
                        YouTubeMusicTrack(
                            videoId = current.videoId!!,
                            title = current.title,
                            artist = current.artist,
                            album = current.album,
                            artworkUrl = current.artworkUrl,
                            durationSeconds = current.durationMs?.div(1_000L)?.toInt(),
                        ),
                    )
                }
            }.filter { it.videoId.isNotBlank() }.distinctBy(YouTubeMusicTrack::videoId)
        }
    }

    if (state.isLoading && tracks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (tracks.isEmpty()) {
        FlowEmptyState(onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings)
        return
    }

    val pagerState = rememberPagerState(pageCount = { tracks.size })

    // Keep the visual Flow page in lockstep with automatic queue advances.
    var autoAdvanceTarget by remember { mutableStateOf<Int?>(null) }
    val currentPlaybackId by rememberUpdatedState(playback.current?.videoId)
    LaunchedEffect(playback.current?.videoId, tracks) {
        val currentId = playback.current?.videoId ?: return@LaunchedEffect
        val currentIndex = tracks.indexOfFirst { it.videoId == currentId }
        if (currentIndex >= 0 && currentIndex != pagerState.currentPage) {
            autoAdvanceTarget = currentIndex
            pagerState.animateScrollToPage(currentIndex)
        }
    }

    // Only a user-selected settled page starts a different queue item.
    // Programmatic auto-advance scrolls are consumed by autoAdvanceTarget.
    LaunchedEffect(pagerState, tracks) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            // The initial settled page is restored UI state, not a user
            // swipe. Replaying it used to replace the active Home song with
            // Flow's first recommendation whenever Flow was re-entered
            // (for example after returning from Settings).
            .drop(1)
            .collect { page ->
                if (autoAdvanceTarget == page) {
                    autoAdvanceTarget = null
                } else {
                    val track = tracks.getOrNull(page) ?: return@collect
                    if (track.videoId != currentPlaybackId) {
                        musicPlayer.playQueue(
                            tracks = tracks.map(YouTubeMusicTrack::toPlayableTrack),
                            startIndex = page,
                            sourceLabel = "Flow",
                            enableRadioContinuation = true,
                        )
                    }
                }
            }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            FlowPage(
                track = tracks[page],
                isPlaying = playback.current?.videoId == tracks[page].videoId && playback.isPlaying,
                onTogglePlayback = musicPlayer::togglePlayPause,
                isLiked = tracks[page].toPlayableTrack().toGeneratedTrack().isLikedIn(likedKeys),
                onToggleLike = { playerViewModel.toggleLiked(tracks[page].toPlayableTrack()) },
                onOpenArtist = playerViewModel::openArtist,
                onOpenMenu = { menuTrack = tracks[page] },
                onOpenLyrics = openPlayerLyrics,
                partyTorchEnabled = partyTorchEnabled,
                onPartyTorchEnabledChange = { partyTorchEnabled = it },
                audioSessionId = musicPlayer.activePlayer?.audioSessionId ?: 0,
                artworkStyle = artworkStyle,
            )
        }
        menuTrack?.let { track ->
            TrackContextMenuSheet(
                target = TrackMenuTarget.Track(track.title, track.artist, "https://music.youtube.com/watch?v=${track.videoId}"),
                capabilities = TrackMenuCapabilities(showDeleteScrobble = false, showRecommendationExclusion = true),
                playableTrack = track.toPlayableTrack(),
                playbackSourceLabel = "Flow",
                onDismiss = { menuTrack = null },
                onPlayInSYMPHONY = { musicPlayer.play(track.toPlayableTrack(), sourceLabel = "Flow") },
            )
        }
        FlowTopBar(
            onOpenSearch = onOpenSearch,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
private fun FlowPage(
    track: YouTubeMusicTrack,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenLyrics: () -> Unit,
    partyTorchEnabled: Boolean,
    onPartyTorchEnabledChange: (Boolean) -> Unit,
    audioSessionId: Int,
    artworkStyle: FlowArtworkStyle,
) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.94f),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.48f),
                        0.38f to Color.Black.copy(alpha = 0.08f),
                        0.62f to Color.Black.copy(alpha = 0.20f),
                        1f to Color.Black.copy(alpha = 0.82f),
                    ),
                ),
        )
        FlowArtworkBorderOverlay(style = artworkStyle)
        if (isPlaying) FlowFloatingHearts()
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 18.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist.ifBlank { "Unknown artist" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { track.artist.takeIf(String::isNotBlank)?.let(onOpenArtist) },
            )
            if (!track.album.isNullOrBlank()) Text(track.album, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.48f), onClick = onToggleLike, modifier = Modifier.size(54.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "${if (isLiked) "Unlike" else "Like"}", tint = if (isLiked) Color(0xFFFF6B81) else Color.White, modifier = Modifier.size(27.dp))
                    }
                }
                Surface(shape = CircleShape, color = scheme.surface.copy(alpha = 0.34f), onClick = onOpenMenu, modifier = Modifier.size(54.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.MoreVert, "More options", tint = Color.White, modifier = Modifier.size(27.dp)) }
                }
                Surface(shape = CircleShape, color = scheme.surface.copy(alpha = 0.34f), onClick = onOpenLyrics, modifier = Modifier.size(54.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.FormatQuote, "Open lyrics", tint = scheme.onPrimary, modifier = Modifier.size(27.dp)) }
                }
            }
            FlowPartyTorch(
                enabled = partyTorchEnabled,
                isPlaying = isPlaying,
                audioSessionId = audioSessionId,
                onEnabledChange = onPartyTorchEnabledChange,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Surface(
            shape = CircleShape,
            color = scheme.primary.copy(alpha = 0.96f),
            shadowElevation = 20.dp,
            onClick = onTogglePlayback,
            modifier = Modifier.align(Alignment.Center).size(88.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(42.dp),
                )
            }
        }
    }
}



@Composable
private fun FlowArtworkBorderOverlay(style: FlowArtworkStyle) {
    if (style == FlowArtworkStyle.NONE) return
    val colors = when (style) {
        FlowArtworkStyle.NONE -> emptyList()
        FlowArtworkStyle.TITANIUM -> listOf(Color(0xFFE7EDF5), Color(0xFF778397), Color(0xFFF8FAFC))
        FlowArtworkStyle.CRYSTAL -> listOf(Color(0xFFB9F2FF), Color(0xFF6AC8F2), Color(0xFFE4D9FF))
        FlowArtworkStyle.TITANIUM -> listOf(Color(0xFF9C75FF), Color(0xFF54DDF2), Color(0xFFE0B7FF))
        FlowArtworkStyle.OBSIDIAN -> listOf(Color(0xFF151922), Color(0xFF8D98AA), Color(0xFF303846))
        FlowArtworkStyle.PRISM -> listOf(Color(0xFF67E8F9), Color(0xFFB58AFF), Color(0xFFFFA6C9))
    }
    Box(Modifier.fillMaxSize().padding(7.dp)) {
        Box(
            Modifier.fillMaxSize()
                .border(1.35.dp, Brush.linearGradient(colors), RoundedCornerShape(25.dp))
                .padding(3.dp)
                .border(0.65.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(22.dp)),
        )
    }
}

@Composable
private fun FlowTopBar(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val foreground = Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "FLOW",
            modifier = Modifier.weight(1f),
            color = foreground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f), onClick = onOpenSearch, modifier = Modifier.size(52.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Search, "Search", tint = foreground, modifier = Modifier.size(27.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f), onClick = onOpenSettings, modifier = Modifier.size(52.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Settings, "Settings", tint = foreground, modifier = Modifier.size(27.dp))
            }
        }
    }
}

@Composable
private fun FlowEmptyState(onOpenSearch: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Your Flow is ready when music is available", style = MaterialTheme.typography.titleLarge)
        Text(
            "Search for a song or connect your music account to fill it with recommendations.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.padding(top = 20.dp)) {
            IconButton(onClick = onOpenSearch) { Icon(Icons.Filled.Search, "Search") }
            IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Settings") }
        }
    }
}

@Composable
private fun FlowFloatingHearts() {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "flowHearts")
    val phases = listOf(0, 620, 1240)
    Box(
        modifier = Modifier.fillMaxSize().padding(end = 22.dp, bottom = 150.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        phases.forEachIndexed { index, delayMs ->
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(
                        durationMillis = 2900,
                        delayMillis = delayMs,
                        easing = androidx.compose.animation.core.LinearEasing,
                    ),
                ),
                label = "flowHeart$index",
            )
            Text(
                text = if (index == 1) "♥" else "❤",
                color = Color(0xFFFF6B81),
                fontSize = (20 + index * 4).sp,
                modifier = Modifier.graphicsLayer {
                    translationY = -progress * 285.dp.toPx()
                    translationX = (index - 1) * 18.dp.toPx() + kotlin.math.sin(progress * Math.PI * 2).toFloat() * 10.dp.toPx()
                    alpha = (1f - progress).coerceIn(0f, 1f)
                    scaleX = 0.75f + progress * 0.35f
                    scaleY = 0.75f + progress * 0.35f
                },
            )
        }
    }
}
