package com.symphony.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AccentMode(val storageValue: String) {
    MANUAL("manual"),
    DYNAMIC("dynamic"),
    MONOCHROME("monochrome");

    companion object {
        fun fromStorage(value: String?): AccentMode =
            entries.firstOrNull { it.storageValue == value } ?: MANUAL
    }
}

enum class FlowArtworkStyle(val storageValue: String, val title: String, val description: String) {
    NONE("none", "No Border", "Show the original artwork without a frame"),
    TITANIUM("titanium", "Titanium", "Precision-cut silver frame with a soft metallic edge"),
    CRYSTAL("crystal", "Crystal", "Clear polished glass with cool refracted highlights"),
    AURORA("aurora", "Aurora", "Violet and cyan luminous edge accents"),
    OBSIDIAN("obsidian", "Obsidian", "Deep graphite frame with a restrained inner sheen"),
    PRISM("prism", "Prism", "Subtle multi-tone spectrum along the artwork edge");

    companion object {
        fun fromStorage(value: String?): FlowArtworkStyle =
            entries.firstOrNull { it.storageValue == value } ?: TITANIUM
    }
}

enum class FlowAtmosphereEffect(
    val storageId: String,
    val title: String,
    val description: String,
) {
    HEARTFALL("heartfall", "Heartfall", "Slow floating red hearts across the Flow screen"),
    AUDIO_REACTIVE_AURA("audio_aura", "Audio Reactive Aura", "Subtle edge light that follows the music rhythm"),
    CINEMATIC_MOTION("cinematic_motion", "Cinematic Motion", "Slow, gentle artwork zoom with smooth track changes"),
    STARDUST("stardust", "Stardust", "Tiny drifting light particles and occasional glints"),
    AURORA_GLOW("aurora_glow", "Aurora Flow", "Soft violet and cyan light near the screen edges"),
    LIGHT_SWEEP("light_sweep", "Light Sweep", "An occasional soft cinematic highlight across the artwork"),
    DREAMY_BOKEH("dreamy_bokeh", "Dreamy Bokeh", "Slow, translucent ambient light circles");

    companion object {
        fun fromStorage(value: String): FlowAtmosphereEffect? =
            entries.firstOrNull { it.storageId == value }
    }
}

enum class FlowAtmosphereIntensity(val storageId: String, val title: String, val multiplier: Float) {
    LOW("low", "Low", 0.72f),
    BALANCED("balanced", "Balanced", 1f),
    HIGH("high", "High", 1.22f);

    companion object {
        fun fromStorage(value: String?): FlowAtmosphereIntensity =
            entries.firstOrNull { it.storageId == value } ?: BALANCED
    }
}

enum class FlowAtmosphereDensity(val storageId: String, val title: String, val multiplier: Float) {
    LOW("low", "Low", 0.65f),
    MEDIUM("medium", "Medium", 1f),
    HIGH("high", "High", 1.45f);

    companion object {
        fun fromStorage(value: String?): FlowAtmosphereDensity =
            entries.firstOrNull { it.storageId == value } ?: MEDIUM
    }
}

enum class FlowAudioSensitivity(val storageId: String, val title: String, val multiplier: Float) {
    LOW("low", "Low", 0.72f),
    BALANCED("balanced", "Balanced", 1f),
    HIGH("high", "High", 1.3f);

    companion object {
        fun fromStorage(value: String?): FlowAudioSensitivity =
            entries.firstOrNull { it.storageId == value } ?: BALANCED
    }
}

data class FlowAtmosphereSettings(
    val enabled: Boolean = true,
    val effects: Set<FlowAtmosphereEffect> = DEFAULT_EFFECTS,
    val intensity: FlowAtmosphereIntensity = FlowAtmosphereIntensity.BALANCED,
    val density: FlowAtmosphereDensity = FlowAtmosphereDensity.MEDIUM,
    val audioSensitivity: FlowAudioSensitivity = FlowAudioSensitivity.BALANCED,
) {
    companion object {
        val DEFAULT_EFFECTS: Set<FlowAtmosphereEffect> = setOf(
            FlowAtmosphereEffect.HEARTFALL,
            FlowAtmosphereEffect.AUDIO_REACTIVE_AURA,
            FlowAtmosphereEffect.CINEMATIC_MOTION,
            FlowAtmosphereEffect.STARDUST,
        )
        val DEFAULT = FlowAtmosphereSettings()

        fun fromStorage(
            enabled: Boolean?,
            effects: Set<String>?,
            intensity: String?,
            density: String?,
            audioSensitivity: String?,
        ): FlowAtmosphereSettings {
            val parsedEffects = effects?.mapNotNull(FlowAtmosphereEffect::fromStorage)?.toSet()
                ?: DEFAULT_EFFECTS
            return FlowAtmosphereSettings(
                enabled = enabled ?: true,
                effects = parsedEffects,
                intensity = FlowAtmosphereIntensity.fromStorage(intensity),
                density = FlowAtmosphereDensity.fromStorage(density),
                audioSensitivity = FlowAudioSensitivity.fromStorage(audioSensitivity),
            )
        }
    }
}

enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

data class ThemePrefs(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: String = "#E03030",
    val accentLight: String = "#FF6060",
    val accentMode: AccentMode = AccentMode.MANUAL,
    val amoled: Boolean = false,
    /** iOS-style liquid-glass materials. Enabled by default. */
    val liquidGlass: Boolean = true,
    /** Optional compact Material 3 floating dock instead of the standard bottom tabs. */
    val md3NavBar: Boolean = false,
    /** Whether bottom navigation tabs are completely hidden. */
    val hideNavTabs: Boolean = false,
    /** Haptic feedback on taps, buttons, toggles and gestures across the app. */
    val uiHaptics: Boolean = true,
    /** Dedicated SYMPHONY Signature Theme inspired by the app logo icon (#C9FB00 & #012226). */
    val symphonyTheme: Boolean = false,
    val useLegacyColors: Boolean = false,
    val appIcon: AppIcon = AppIcon.DEFAULT,
    val flowArtworkStyle: FlowArtworkStyle = FlowArtworkStyle.TITANIUM,
    val flowAtmosphere: FlowAtmosphereSettings = FlowAtmosphereSettings.DEFAULT,
)

@Singleton
class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("lw_themeMode")
        val ACCENT_COLOR = stringPreferencesKey("lw_accent")
        val ACCENT_LIGHT = stringPreferencesKey("lw_accentLight")
        val ACCENT_MODE = stringPreferencesKey("lw_accentMode")
        val AMOLED = booleanPreferencesKey("lw_amoled")
        val LIQUID_GLASS = booleanPreferencesKey("lw_liquidGlass")
        val MD3_NAV_BAR = booleanPreferencesKey("lw_md3NavBar")
        val HIDE_NAV_TABS = booleanPreferencesKey("lw_hideNavTabs")
        val UI_HAPTICS = booleanPreferencesKey("lw_ui_haptics")
        val SYMPHONY_THEME = booleanPreferencesKey("lw_symphony_theme")
        val USE_LEGACY_COLORS = booleanPreferencesKey("lw_useLegacyColors")
        val APP_ICON = stringPreferencesKey("lw_app_icon")
        val FLOW_ARTWORK_STYLE = stringPreferencesKey("lw_flow_artwork_style")
        val FLOW_ATMOSPHERE_ENABLED = booleanPreferencesKey("lw_flow_atmosphere_enabled")
        val FLOW_ATMOSPHERE_EFFECTS = stringSetPreferencesKey("lw_flow_atmosphere_effects")
        val FLOW_ATMOSPHERE_INTENSITY = stringPreferencesKey("lw_flow_atmosphere_intensity")
        val FLOW_ATMOSPHERE_DENSITY = stringPreferencesKey("lw_flow_atmosphere_density")
        val FLOW_ATMOSPHERE_SENSITIVITY = stringPreferencesKey("lw_flow_atmosphere_sensitivity")
    }

    val prefs: Flow<ThemePrefs> = dataStore.data
        .recoverPreferences("ThemePreferences")
        .map { p ->
            ThemePrefs(
                themeMode = ThemeMode.fromStorage(p.readSafely(Keys.THEME_MODE)),
                accentColor = p.readSafely(Keys.ACCENT_COLOR) ?: "#E03030",
                accentLight = p.readSafely(Keys.ACCENT_LIGHT) ?: "#FF6060",
                accentMode = AccentMode.fromStorage(p.readSafely(Keys.ACCENT_MODE)),
                amoled = p.readSafely(Keys.AMOLED) ?: false,
                liquidGlass = p.readSafely(Keys.LIQUID_GLASS) ?: true,
                md3NavBar = p.readSafely(Keys.MD3_NAV_BAR) ?: false,
                hideNavTabs = p.readSafely(Keys.HIDE_NAV_TABS) ?: false,
                uiHaptics = p.readSafely(Keys.UI_HAPTICS) ?: true,
                symphonyTheme = p.readSafely(Keys.SYMPHONY_THEME) ?: false,
                useLegacyColors = p.readSafely(Keys.USE_LEGACY_COLORS) ?: false,
                appIcon = AppIcon.fromId(p.readSafely(Keys.APP_ICON)),
                flowArtworkStyle = FlowArtworkStyle.fromStorage(p.readSafely(Keys.FLOW_ARTWORK_STYLE)),
                flowAtmosphere = FlowAtmosphereSettings.fromStorage(
                    enabled = p[Keys.FLOW_ATMOSPHERE_ENABLED],
                    effects = p[Keys.FLOW_ATMOSPHERE_EFFECTS],
                    intensity = p.readSafely(Keys.FLOW_ATMOSPHERE_INTENSITY),
                    density = p.readSafely(Keys.FLOW_ATMOSPHERE_DENSITY),
                    audioSensitivity = p.readSafely(Keys.FLOW_ATMOSPHERE_SENSITIVITY),
                ),
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.storageValue }
    }

    suspend fun setManualAccent(color: String, light: String) {
        dataStore.edit {
            it[Keys.ACCENT_COLOR] = color
            it[Keys.ACCENT_LIGHT] = light
            it[Keys.ACCENT_MODE] = AccentMode.MANUAL.storageValue
            it[Keys.SYMPHONY_THEME] = false
        }
    }

    suspend fun setMode(mode: AccentMode) {
        dataStore.edit {
            it[Keys.ACCENT_MODE] = mode.storageValue
            if (mode == AccentMode.DYNAMIC || mode == AccentMode.MONOCHROME) {
                it[Keys.SYMPHONY_THEME] = false
            }
        }
    }

    suspend fun setAmoled(enabled: Boolean) {
        dataStore.edit { it[Keys.AMOLED] = enabled }
    }

    suspend fun setLiquidGlass(enabled: Boolean) {
        dataStore.edit { it[Keys.LIQUID_GLASS] = enabled }
    }

    suspend fun setMd3NavBar(enabled: Boolean) {
        dataStore.edit { it[Keys.MD3_NAV_BAR] = enabled }
    }

    suspend fun setHideNavTabs(enabled: Boolean) {
        dataStore.edit { it[Keys.HIDE_NAV_TABS] = enabled }
    }

    suspend fun setUiHaptics(enabled: Boolean) {
        dataStore.edit { it[Keys.UI_HAPTICS] = enabled }
    }

    suspend fun setLegacyColors(enabled: Boolean) {
        dataStore.edit { it[Keys.USE_LEGACY_COLORS] = enabled }
    }

    suspend fun setSYMPHONYTheme(enabled: Boolean) {
        dataStore.edit {
            it[Keys.SYMPHONY_THEME] = enabled
            if (enabled) {
                it[Keys.ACCENT_COLOR] = "#C9FB00"
                it[Keys.ACCENT_LIGHT] = "#C9FB00"
                it[Keys.ACCENT_MODE] = AccentMode.MANUAL.storageValue
            }
        }
    }

    suspend fun setFlowArtworkStyle(style: FlowArtworkStyle) {
        dataStore.edit { it[Keys.FLOW_ARTWORK_STYLE] = style.storageValue }
    }

    suspend fun setFlowAtmosphere(settings: FlowAtmosphereSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.FLOW_ATMOSPHERE_ENABLED] = settings.enabled
            preferences[Keys.FLOW_ATMOSPHERE_EFFECTS] = settings.effects.map { it.storageId }.toSet()
            preferences[Keys.FLOW_ATMOSPHERE_INTENSITY] = settings.intensity.storageId
            preferences[Keys.FLOW_ATMOSPHERE_DENSITY] = settings.density.storageId
            preferences[Keys.FLOW_ATMOSPHERE_SENSITIVITY] = settings.audioSensitivity.storageId
        }
    }

    suspend fun setAppIcon(icon: AppIcon) {
        dataStore.edit { it[Keys.APP_ICON] = icon.id }
    }
}
