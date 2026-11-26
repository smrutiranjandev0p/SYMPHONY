package com.symphony.app.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.symphony.app.data.local.FlowAtmosphereDensity
import com.symphony.app.data.local.FlowAtmosphereEffect
import com.symphony.app.data.local.FlowAtmosphereSettings
import com.symphony.app.playback.rhythm.FluidRhythmEngine
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FlowParticleSeed(
    val x: Float,
    val y: Float,
    val phase: Float,
    val speed: Float,
    val size: Float,
    val drift: Float,
)

private fun particleSeeds(count: Int, seed: Int): List<FlowParticleSeed> {
    val random = Random(seed)
    return List(count) {
        FlowParticleSeed(
            x = random.nextFloat(),
            y = random.nextFloat(),
            phase = random.nextFloat(),
            speed = 0.045f + random.nextFloat() * 0.075f,
            size = random.nextFloat(),
            drift = 0.012f + random.nextFloat() * 0.035f,
        )
    }
}

private val FLOW_HEART_PATH = Path().apply {
    moveTo(50f, 91f)
    cubicTo(42f, 83f, 5f, 57f, 5f, 31f)
    cubicTo(5f, 8f, 34f, -2f, 50f, 22f)
    cubicTo(66f, -2f, 95f, 8f, 95f, 31f)
    cubicTo(95f, 57f, 58f, 83f, 50f, 91f)
    close()
}

/**
 * Slow cinematic zoom is applied to the artwork image layer only. The image is
 * clipped to its original full-screen bounds; controls and Flow layout are not
 * transformed.
 */
@Composable
internal fun Modifier.flowCinematicMotion(
    enabled: Boolean,
    isCurrentPage: Boolean,
): Modifier {
    val lifecycleOwner = LocalLifecycleOwner.current
    var foreground by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> foreground = true
                Lifecycle.Event.ON_STOP -> foreground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scale = remember { Animatable(1f) }
    val active = enabled && isCurrentPage && foreground
    LaunchedEffect(active) {
        if (!active) {
            scale.animateTo(1f, animationSpec = tween(500))
            return@LaunchedEffect
        }
        while (isActive) {
            scale.animateTo(1.035f, animationSpec = tween(18_000, easing = LinearEasing))
            scale.animateTo(1f, animationSpec = tween(18_000, easing = LinearEasing))
        }
    }

    return this
        .clipToBounds()
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}

/**
 * Flow-only atmosphere compositor. A single 30 fps Canvas draws all optional
 * particles and light fields; it never intercepts touch input. Audio Aura
 * reuses SYMPHONY's existing PCM rhythm analyser through FluidRhythmEngine,
 * so it needs no microphone permission and does not touch the audio signal.
 */
@Composable
internal fun FlowAtmosphereOverlay(
    settings: FlowAtmosphereSettings,
    isPlaying: Boolean,
    isCurrentPage: Boolean,
    isForeground: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    val activeEffects = settings.effects
    val shouldAnimate = settings.enabled && activeEffects.isNotEmpty() && isCurrentPage && isForeground
    val audioAuraActive = shouldAnimate && FlowAtmosphereEffect.AUDIO_REACTIVE_AURA in activeEffects

    val audioAuraActive = shouldAnimate && FlowAtmosphereEffect.AUDIO_REACTIVE_AURA in activeEffects

    val heartSeeds = remember { particleSeeds(24, 0x51A7) }
    val dustSeeds = remember { particleSeeds(52, 0xD057) }
    val bokehSeeds = remember { particleSeeds(12, 0xB0CE) }
    val glowSeeds = remember { particleSeeds(4, 0xA220) }

    var animationSeconds by remember { mutableFloatStateOf(0f) }
    var audioLevel by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldAnimate, audioAuraActive, isPlaying) {
        if (!shouldAnimate) {
            audioLevel = 0f
            return@LaunchedEffect
        }
        var lastFrame = withFrameNanos { it }
        var frameCount = 0
        while (isActive) {
            val now = withFrameNanos { it }
            frameCount++
            // Update drawing state every second frame (approximately 30 fps).
            if (frameCount % 2 == 0) {
                val dt = ((now - lastFrame) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
                lastFrame = now
                animationSeconds = (animationSeconds + dt) % 120f
                audioLevel = if (audioAuraActive) {
                    FluidRhythmEngine.tick(dt, isPlaying)
                } else {
                    0f
                }
            }
        }
    }

    if (!shouldAnimate) return

    Canvas(modifier = modifier) {
        val time = animationSeconds
        val intensity = settings.intensity.multiplier
        val density = settings.density
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        if (FlowAtmosphereEffect.HEARTFALL in activeEffects) {
            val heartCount = when (density) {
                FlowAtmosphereDensity.LOW -> 8
                FlowAtmosphereDensity.MEDIUM -> 14
                FlowAtmosphereDensity.HIGH -> 21
            }
            val heartColors = listOf(Color(0xFFFF526F), Color(0xFFE83D62), Color(0xFFFF8297), Color(0xFFD92F55))
            heartSeeds.take(heartCount).forEachIndexed { index, particle ->
                val progress = (time * particle.speed + particle.phase) % 1f
                val x = (particle.x + sin(time * 0.34f + particle.phase * 6.28f) * particle.drift) * width
                val y = height * (1f - progress)
                val heartSize = (10f + particle.size * 12f) * intensity
                val fade = (sin(progress * PI).toFloat().coerceAtLeast(0f) * 0.58f + 0.08f)
                val alpha = (fade * (0.56f + particle.size * 0.34f) * intensity).coerceIn(0f, 0.62f)
                val rotation = sin(time * 0.52f + particle.phase * 6.28f) * 9f
                val color = heartColors[index % heartColors.size]
                withTransform({
                    translate(x - heartSize / 2f, y - heartSize / 2f)
                    rotate(rotation, pivot = Offset(heartSize / 2f, heartSize / 2f))
                    scale(heartSize / 100f, heartSize / 100f, pivot = Offset.Zero)
                }) {
                    drawPath(FLOW_HEART_PATH, color.copy(alpha = alpha * 0.18f))
                    drawPath(FLOW_HEART_PATH, color.copy(alpha = alpha))
                }
            }
        }

        if (FlowAtmosphereEffect.STARDUST in activeEffects) {
            val dustCount = when (density) {
                FlowAtmosphereDensity.LOW -> 14
                FlowAtmosphereDensity.MEDIUM -> 26
                FlowAtmosphereDensity.HIGH -> 40
            }
            dustSeeds.take(dustCount).forEachIndexed { index, particle ->
                val travel = (particle.y - time * particle.speed * 0.24f + 2f) % 1f
                val x = (particle.x + sin(time * 0.22f + particle.phase * 6.28f) * particle.drift * 0.5f) * width
                val y = travel * height
                val twinkle = (0.5f + 0.5f * sin(time * (0.75f + particle.speed * 4f) + particle.phase * 6.28f))
                val alpha = (0.08f + twinkle * 0.22f) * intensity
                val radius = (0.75f + particle.size * 1.35f) * intensity
                val tint = if (index % 3 == 0) secondaryColor else Color.White
                drawCircle(tint.copy(alpha = alpha.coerceAtMost(0.34f)), radius.dp.toPx(), Offset(x, y))
                if (index % 9 == 0 && twinkle > 0.78f) {
                    val ray = radius.dp.toPx() * 2.4f
                    drawLine(Color.White.copy(alpha = alpha * 0.42f), Offset(x - ray, y), Offset(x + ray, y), strokeWidth = 0.65.dp.toPx())
                    drawLine(Color.White.copy(alpha = alpha * 0.42f), Offset(x, y - ray), Offset(x, y + ray), strokeWidth = 0.65.dp.toPx())
                }
            }
        }

        if (FlowAtmosphereEffect.AURORA_GLOW in activeEffects) {
            val radius = maxOf(width, height) * 0.42f
            glowSeeds.forEachIndexed { index, particle ->
                val center = when (index) {
                    0 -> Offset(width * 0.02f, height * 0.24f)
                    1 -> Offset(width * 0.98f, height * 0.36f)
                    2 -> Offset(width * 0.05f, height * 0.82f)
                    else -> Offset(width * 0.92f, height * 0.84f)
                }
                val drift = sin(time * 0.18f + particle.phase * 6.28f) * 0.035f * width
                val color = if (index % 2 == 0) primaryColor else secondaryColor
                val glowAlpha = (0.045f + 0.025f * sin(time * 0.31f + particle.phase * 6.28f)) * intensity
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = glowAlpha.coerceAtLeast(0.01f)), color.copy(alpha = 0f)),
                        center = Offset(center.x + drift, center.y),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(center.x + drift, center.y),
                )
            }
        }

        if (FlowAtmosphereEffect.DREAMY_BOKEH in activeEffects) {
            val bokehCount = when (density) {
                FlowAtmosphereDensity.LOW -> 3
                FlowAtmosphereDensity.MEDIUM -> 5
                FlowAtmosphereDensity.HIGH -> 8
            }
            bokehSeeds.take(bokehCount).forEachIndexed { index, particle ->
                val x = (particle.x + sin(time * 0.12f + particle.phase * 6.28f) * particle.drift * 1.6f) * width
                val y = (particle.y + cos(time * 0.15f + particle.phase * 6.28f) * 0.045f) * height
                val radius = (12f + particle.size * 28f) * intensity
                val color = if (index % 2 == 0) secondaryColor else primaryColor
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.13f), color.copy(alpha = 0.045f), Color.Transparent),
                        center = Offset(x, y),
                        radius = radius.dp.toPx(),
                    ),
                    radius = radius.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }

        if (FlowAtmosphereEffect.AUDIO_REACTIVE_AURA in activeEffects && isPlaying) {
            val drive = (audioLevel * settings.audioSensitivity.multiplier).coerceIn(0f, 1f)
            if (drive > 0.015f) {
                val inset = 7.dp.toPx()
                val glowAlpha = (0.045f + drive * 0.20f) * intensity
                val borderWidth = (1.15f + drive * 2.8f) * intensity
                val brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha.coerceAtMost(0.32f)),
                        secondaryColor.copy(alpha = (glowAlpha * 0.82f).coerceAtMost(0.28f)),
                        primaryColor.copy(alpha = (glowAlpha * 0.62f).coerceAtMost(0.24f)),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(width, height),
                )
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(inset, inset),
                    size = Size(width - inset * 2f, height - inset * 2f),
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    style = Stroke(width = borderWidth.dp.toPx()),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = (drive * 0.095f * intensity).coerceAtMost(0.13f)),
                    topLeft = Offset(inset + 2.dp.toPx(), inset + 2.dp.toPx()),
                    size = Size(width - (inset + 2.dp.toPx()) * 2f, height - (inset + 2.dp.toPx()) * 2f),
                    cornerRadius = CornerRadius(23.dp.toPx()),
                    style = Stroke(width = 0.7.dp.toPx()),
                )
            }
        }

        if (FlowAtmosphereEffect.LIGHT_SWEEP in activeEffects) {
            val sweepDuration = 1.6f
            val cycle = time % 16f
            if (cycle < sweepDuration) {
                val progress = cycle / sweepDuration
                val sweepWidth = width * 0.28f
                val left = -sweepWidth + progress * (width + sweepWidth * 2f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.045f * intensity),
                            secondaryColor.copy(alpha = 0.035f * intensity),
                            Color.Transparent,
                        ),
                        start = Offset(left, 0f),
                        end = Offset(left + sweepWidth, height),
                    ),
                    topLeft = Offset(left, 0f),
                    size = Size(sweepWidth, height),
                )
            }
        }
    }
}
