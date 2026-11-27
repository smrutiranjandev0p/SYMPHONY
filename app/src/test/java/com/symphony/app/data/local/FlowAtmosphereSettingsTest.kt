package com.symphony.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowAtmosphereSettingsTest {

    @Test
    fun defaultAtmosphereStartsWithHeartfallAndCoreVisualEffects() {
        val settings = FlowAtmosphereSettings.DEFAULT

        assertTrue(settings.enabled)
        assertTrue(FlowAtmosphereEffect.HEARTFALL in settings.effects)
        assertTrue(FlowAtmosphereEffect.AUDIO_REACTIVE_AURA in settings.effects)
        assertTrue(FlowAtmosphereEffect.CINEMATIC_MOTION in settings.effects)
        assertTrue(FlowAtmosphereEffect.STARDUST in settings.effects)
        assertEquals(FlowAtmosphereIntensity.BALANCED, settings.intensity)
        assertEquals(FlowAtmosphereDensity.MEDIUM, settings.density)
    }

    @Test
    fun parsesSavedOptionsAndIgnoresUnknownEffectIds() {
        val settings = FlowAtmosphereSettings.fromStorage(
            enabled = false,
            effects = setOf("heartfall", "audio_aura", "future_effect"),
            intensity = "high",
            density = "low",
            audioSensitivity = "high",
        )

        assertFalse(settings.enabled)
        assertEquals(
            setOf(
                FlowAtmosphereEffect.HEARTFALL,
                FlowAtmosphereEffect.AUDIO_REACTIVE_AURA,
            ),
            settings.effects,
        )
        assertEquals(FlowAtmosphereIntensity.HIGH, settings.intensity)
        assertEquals(FlowAtmosphereDensity.LOW, settings.density)
        assertEquals(FlowAudioSensitivity.HIGH, settings.audioSensitivity)
    }

    @Test
    fun missingStoredValuesUseSafeDefaults() {
        assertEquals(
            FlowAtmosphereSettings.DEFAULT,
            FlowAtmosphereSettings.fromStorage(
                enabled = null,
                effects = null,
                intensity = null,
                density = null,
                audioSensitivity = null,
            ),
        )
    }

    @Test
    fun anEmptySavedEffectSetRemainsAnIntentionalNoEffectsSelection() {
        val settings = FlowAtmosphereSettings.fromStorage(
            enabled = true,
            effects = emptySet(),
            intensity = "balanced",
            density = "medium",
            audioSensitivity = "balanced",
        )

        assertTrue(settings.enabled)
        assertTrue(settings.effects.isEmpty())
    }
}
