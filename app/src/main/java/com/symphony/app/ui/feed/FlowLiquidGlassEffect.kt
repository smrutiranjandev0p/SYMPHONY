package com.symphony.app.ui.feed

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.symphony.app.data.local.FlowArtworkStyle

/**
 * Applies a GPU optical-glass treatment to the artwork RenderNode itself.
 *
 * On Android 13+ AGSL samples the original artwork to create restrained
 * refraction, local diffusion and Fresnel edge reflections. No large
 * decorative gradient is painted over the screen. Older Android releases
 * keep the original artwork untouched rather than showing a fake overlay.
 */
@Composable
internal fun Modifier.flowLiquidGlassEffect(
    style: FlowArtworkStyle,
    isPlaying: Boolean,
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return this

    val shader = remember {
        runCatching { RuntimeShader(FLOW_LIQUID_GLASS_AGSL) }
            .onFailure { Log.w("FlowLiquidGlass", "AGSL shader unavailable; using original artwork", it) }
            .getOrNull()
    } ?: return this

    val transition = rememberInfiniteTransition(label = "flowOpticalGlass")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 10500 else 22000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flowOpticalGlassPhase",
    )

    val styleValue = when (style) {
        FlowArtworkStyle.CRYSTAL -> 0f
        FlowArtworkStyle.AURORA -> 1f
        FlowArtworkStyle.OBSIDIAN -> 2f
    }

    SideEffect {
        shader.setFloatUniform("uTime", phase)
        shader.setFloatUniform("uStyle", styleValue)
        shader.setFloatUniform("uPlaying", if (isPlaying) 1f else 0f)
    }

    val effect = remember(shader) {
        RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }

    return this.graphicsLayer {
        shader.setFloatUniform(
            "uResolution",
            size.width.coerceAtLeast(1f),
            size.height.coerceAtLeast(1f),
        )
        renderEffect = effect
    }
}

/**
 * A continuous, very restrained optical material:
 * - sub-pixel lens displacement samples the artwork itself;
 * - a small multi-tap diffusion softens only the refractive regions;
 * - Fresnel reflections catch the screen-cover perimeter;
 * - one soft moving glint travels slowly along the upper glass edge.
 *
 * The source artwork remains the dominant colour and image. The shader does
 * not add a broad tint, a diagonal stripe, or scattered sparkle particles.
 */
private const val FLOW_LIQUID_GLASS_AGSL = """
uniform shader content;
uniform float2 uResolution;
uniform float uTime;
uniform float uStyle;
uniform float uPlaying;

half4 main(float2 fragCoord) {
    float2 resolution = max(uResolution, float2(1.0, 1.0));
    float2 uv = clamp(fragCoord / resolution, float2(0.0, 0.0), float2(1.0, 1.0));
    half4 original = content.eval(fragCoord);

    float edgeDistance = min(
        min(uv.x, 1.0 - uv.x),
        min(uv.y, 1.0 - uv.y)
    );
    float fresnel = pow(1.0 - smoothstep(0.0, 0.105, edgeDistance), 2.0);

    // A broad, slow lens field bends the original image by only a few pixels.
    // It is smooth and continuous, not a visible diagonal colour stripe.
    float waveA = sin(uv.y * 5.2 + uTime * 0.62 + sin(uv.x * 4.0) * 0.58);
    float waveB = cos(uv.x * 4.6 - uTime * 0.46 + cos(uv.y * 3.2) * 0.52);
    float lensCenterX = 0.50 + 0.10 * sin(uTime * 0.19);
    float lensCenterY = 0.48 + 0.12 * cos(uTime * 0.16);
    float2 lensDelta = (uv - float2(lensCenterX, lensCenterY)) / float2(0.72, 0.88);
    float lensField = exp(-dot(lensDelta, lensDelta) * 1.35);

    float bendScale = 0.30 + fresnel * 0.85 + lensField * 0.62;
    float2 displacement = float2(waveA, waveB) * bendScale;
    float2 samplePosition = clamp(
        fragCoord + displacement,
        float2(0.5, 0.5),
        resolution - float2(0.5, 0.5)
    );
    half4 refracted = content.eval(samplePosition);

    // Very light optical diffusion, weighted toward the lens and perimeter.
    half4 blurA = content.eval(clamp(fragCoord + float2(1.15, 0.0), float2(0.5, 0.5), resolution - float2(0.5, 0.5)));
    half4 blurB = content.eval(clamp(fragCoord + float2(-1.15, 0.0), float2(0.5, 0.5), resolution - float2(0.5, 0.5)));
    half4 blurC = content.eval(clamp(fragCoord + float2(0.0, 1.15), float2(0.5, 0.5), resolution - float2(0.5, 0.5)));
    half4 blurD = content.eval(clamp(fragCoord + float2(0.0, -1.15), float2(0.5, 0.5), resolution - float2(0.5, 0.5)));
    half4 diffused = (blurA + blurB + blurC + blurD) * 0.25;

    float refractionMix = 0.22 + fresnel * 0.20 + lensField * 0.13;
    float diffusionMix = 0.025 + fresnel * 0.075 + lensField * 0.035;
    half4 surface = mix(original, refracted, refractionMix);
    surface = mix(surface, diffused, diffusionMix);

    // Style changes only the glass light, never the source image palette.
    float3 rimPrimary;
    float3 rimSecondary;
    if (uStyle < 0.5) {
        rimPrimary = float3(0.70, 0.90, 1.00);  // Crystal: cool optical glass
        rimSecondary = float3(0.78, 0.72, 1.00);
    } else if (uStyle < 1.5) {
        rimPrimary = float3(0.72, 0.52, 1.00);  // Aurora: restrained violet
        rimSecondary = float3(0.40, 0.84, 1.00);
    } else {
        rimPrimary = float3(0.82, 0.88, 0.98);  // Obsidian: polished graphite
        rimSecondary = float3(0.55, 0.66, 0.82);
    }

    float topRim = exp(-uv.y * 78.0);
    float bottomRim = exp(-(1.0 - uv.y) * 70.0);
    float leftRim = exp(-uv.x * 72.0);
    float rightRim = exp(-(1.0 - uv.x) * 72.0);

    float perimeterLight = fresnel * 0.095;
    float directionalLight =
        topRim * (0.035 + 0.025 * (0.5 + 0.5 * sin(uv.x * 5.0 + uTime * 0.24))) +
        bottomRim * 0.018 +
        leftRim * 0.025 +
        rightRim * 0.022;

    float3 reflection = rimPrimary * (perimeterLight + directionalLight);
    reflection += rimSecondary * (fresnel * 0.035 + (leftRim + rightRim) * 0.012);

    // A single elongated highlight glides around the upper glass edge.
    // It is intentionally soft and intermittent, not a row of sparkles.
    float glintX = 0.50 + 0.34 * sin(uTime * 0.28);
    float glintY = 0.028 + 0.012 * sin(uTime * 0.43);
    float2 glintDelta = (uv - float2(glintX, glintY)) / float2(0.026, 0.0065);
    float glint = exp(-dot(glintDelta, glintDelta)) * (0.18 + 0.20 * uPlaying);

    float3 result = float3(surface.rgb) + reflection + float3(1.0, 1.0, 1.0) * glint;
    return half4(clamp(result, 0.0, 1.0), original.a);
}
"""
