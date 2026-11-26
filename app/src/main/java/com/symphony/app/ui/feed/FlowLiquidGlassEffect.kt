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

    // Preserve the original poster's luminance and colour. Refraction is
    // confined to the glass perimeter rather than washing the full image.
    float edgeDistance = min(
        min(uv.x, 1.0 - uv.x),
        min(uv.y, 1.0 - uv.y)
    );
    float rim = 1.0 - smoothstep(0.0, 0.028, edgeDistance);
    float innerRim = exp(-edgeDistance * 28.0);

    float2 centered = uv - float2(0.5, 0.5);
    float radius = length(centered * float2(0.88, 1.0));
    float lensZone = smoothstep(0.22, 0.70, radius);
    float bend = sin(uv.y * 4.8 + uTime * 0.34 + cos(uv.x * 3.0) * 0.42);
    float2 radial = centered / max(length(centered), 0.001);
    float2 samplePosition = clamp(
        fragCoord + radial * bend * lensZone * 3.2,
        float2(0.5, 0.5),
        resolution - float2(0.5, 0.5)
    );
    half4 refracted = content.eval(samplePosition);
    float refractionAmount = (0.06 + 0.12 * rim) * lensZone;
    half4 opticalImage = mix(original, refracted, refractionAmount);

    // The three styles differ only in their glass reflections.
    float3 primary;
    float3 secondary;
    if (uStyle < 0.5) {
        primary = float3(0.76, 0.94, 1.00);
        secondary = float3(0.88, 0.84, 1.00);
    } else if (uStyle < 1.5) {
        primary = float3(0.72, 0.54, 1.00);
        secondary = float3(0.42, 0.86, 1.00);
    } else {
        primary = float3(0.84, 0.89, 0.98);
        secondary = float3(0.55, 0.63, 0.77);
    }

    // Narrow Fresnel edge and soft inner reflection; no broad haze.
    float3 reflection = primary * (rim * 0.105 + innerRim * 0.012);
    reflection += secondary * (rim * 0.035);

    float topEdge = exp(-uv.y * 190.0);
    float bottomEdge = exp(-(1.0 - uv.y) * 180.0);
    float sideEdge = exp(-uv.x * 190.0) + exp(-(1.0 - uv.x) * 190.0);
    float edgeLight = topEdge * (0.045 + 0.035 * (0.5 + 0.5 * sin(uv.x * 5.2 + uTime * 0.22)));
    edgeLight += bottomEdge * 0.018 + sideEdge * 0.016;
    reflection += primary * edgeLight;

    // One compact glint moves along the upper edge.
    float glintX = 0.5 + 0.34 * sin(uTime * 0.24);
    float2 glintDelta = (uv - float2(glintX, 0.022)) / float2(0.034, 0.0055);
    float glint = exp(-dot(glintDelta, glintDelta)) * (0.13 + 0.12 * uPlaying);
    reflection += float3(1.0, 1.0, 1.0) * glint;

    float3 base = float3(opticalImage.rgb);
    if (uStyle > 1.5) {
        base *= (1.0 - rim * 0.035);
    }

    float3 result = clamp(base + reflection, 0.0, 1.0);
    return half4(result, original.a);
}
"""
