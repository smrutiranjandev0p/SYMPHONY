package com.symphony.app.ui.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
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
    val artworkStyle = appearanceTheme?.flowArtworkStyle ?: FlowArtworkStyle.AURORA
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
            modifier = Modifier.fillMaxSize().alpha(0.94f),
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
        // Keep the animated glass treatment above the artwork and its readability scrim,
        // but below every interactive Flow control.
        FlowArtworkGlassOverlay(style = artworkStyle, isPlaying = isPlaying)
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
private fun FlowArtworkGlassOverlay(
    style: FlowArtworkStyle,
    isPlaying: Boolean,
) {
    // The selected finish animates only over the full-bleed song artwork.
    // Interactive controls are composed after this layer and remain untouched.
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "flowArtworkGlass")
    val sweep by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = if (isPlaying) 6200 else 11000,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "flowArtworkSweep",
    )
    val sparkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = if (isPlaying) 3400 else 6200,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "flowArtworkSparkle",
    )
    val palette = when (style) {
        FlowArtworkStyle.CRYSTAL -> listOf(Color(0xFFBDEBFF), Color(0xFF9C8CFF), Color.White)
        FlowArtworkStyle.AURORA -> listOf(Color(0xFFB36BFF), Color(0xFF55DFFF), Color(0xFFFF83D5))
        FlowArtworkStyle.OBSIDIAN -> listOf(Color(0xFFCBD5E1), Color(0xFF64748B), Color.White)
    }

    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val motionAlpha = if (isPlaying) 1f else 0.52f

        when (style) {
            FlowArtworkStyle.CRYSTAL -> {
                // Cool crystal tint with a clearly visible moving specular reflection.
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(
                        palette[0].copy(alpha = 0.12f),
                        Color.Transparent,
                        palette[1].copy(alpha = 0.10f),
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(w, h),
                ))
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.32f * motionAlpha),
                        palette[0].copy(alpha = 0.18f * motionAlpha),
                        Color.Transparent,
                    ),
                    start = androidx.compose.ui.geometry.Offset(w * (sweep - 0.32f), 0f),
                    end = androidx.compose.ui.geometry.Offset(w * (sweep + 0.20f), h),
                ))
                drawRect(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.09f), Color.Transparent, palette[0].copy(alpha = 0.11f))))
            }
            FlowArtworkStyle.AURORA -> {
                // Purple/cyan aurora ribbons sweep diagonally across the cover.
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(
                        palette[0].copy(alpha = 0.24f),
                        Color.Transparent,
                        palette[1].copy(alpha = 0.20f),
                        Color.Transparent,
                        palette[2].copy(alpha = 0.15f),
                    ),
                    start = androidx.compose.ui.geometry.Offset(w * (0.10f + sweep * 0.12f), 0f),
                    end = androidx.compose.ui.geometry.Offset(w * (0.72f + sweep * 0.10f), h),
                ))
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        palette[1].copy(alpha = 0.30f * motionAlpha),
                        Color.White.copy(alpha = 0.20f * motionAlpha),
                        palette[0].copy(alpha = 0.24f * motionAlpha),
                        Color.Transparent,
                    ),
                    start = androidx.compose.ui.geometry.Offset(w * (sweep - 0.28f), 0f),
                    end = androidx.compose.ui.geometry.Offset(w * (sweep + 0.16f), h),
                ))
            }
            FlowArtworkStyle.OBSIDIAN -> {
                // Smoked obsidian surface with a polished silver moving edge-light.
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.20f), Color.Transparent, palette[0].copy(alpha = 0.14f), Color.Black.copy(alpha = 0.18f)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(w, h),
                ))
                drawRect(brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.26f * motionAlpha), palette[1].copy(alpha = 0.18f * motionAlpha), Color.Transparent),
                    start = androidx.compose.ui.geometry.Offset(w * (sweep - 0.24f), 0f),
                    end = androidx.compose.ui.geometry.Offset(w * (sweep + 0.18f), h),
                ))
            }
        }

        // A fine glass highlight near the perimeter makes the finish readable
        // without changing the artwork's size, crop, or layout.
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette[0].copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.10f),
                    palette[1].copy(alpha = 0.20f),
                    Color.Transparent,
                ),
                start = androidx.compose.ui.geometry.Offset(0f, h * 0.16f),
                end = androidx.compose.ui.geometry.Offset(w, h * 0.84f),
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.15.dp.toPx()),
        )

        // Small, occasional star glints; restrained so they do not obscure cover art.
        val points = listOf(
            0.16f to 0.22f, 0.78f to 0.18f, 0.87f to 0.42f,
            0.24f to 0.58f, 0.69f to 0.72f, 0.43f to 0.84f,
        )
        points.forEachIndexed { index, point ->
            val phase = (sparkle + index * 0.173f) % 1f
            val alpha = (kotlin.math.sin(phase * Math.PI).toFloat().coerceAtLeast(0f) * 0.72f) * motionAlpha
            val radius = if (index % 2 == 0) 2.2.dp.toPx() else 1.6.dp.toPx()
            val center = androidx.compose.ui.geometry.Offset(w * point.first, h * point.second)
            drawCircle(palette[index % palette.size].copy(alpha = alpha), radius, center)
            if (alpha > 0.16f) {
                val ray = radius * 3.2f
                drawLine(Color.White.copy(alpha = alpha * 0.72f), androidx.compose.ui.geometry.Offset(center.x - ray, center.y), androidx.compose.ui.geometry.Offset(center.x + ray, center.y), strokeWidth = 0.9.dp.toPx())
                drawLine(Color.White.copy(alpha = alpha * 0.72f), androidx.compose.ui.geometry.Offset(center.x, center.y - ray), androidx.compose.ui.geometry.Offset(center.x, center.y + ray), strokeWidth = 0.9.dp.toPx())
            }
        }
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
