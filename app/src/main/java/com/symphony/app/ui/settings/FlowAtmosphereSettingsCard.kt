package com.symphony.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.symphony.app.data.local.FlowAtmosphereDensity
import com.symphony.app.data.local.FlowAtmosphereEffect
import com.symphony.app.data.local.FlowAtmosphereIntensity
import com.symphony.app.data.local.FlowAtmosphereSettings
import com.symphony.app.data.local.FlowAudioSensitivity

@Composable
internal fun FlowAtmosphereSettingsCard(
    settings: FlowAtmosphereSettings,
    onSettingsChange: (FlowAtmosphereSettings) -> Unit,
    isHighlighted: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (isHighlighted) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier,
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Flow Atmosphere", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Ambient motion and music-responsive visuals for Flow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
                )
            }

            if (settings.enabled) {
                Text("Effects", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    FlowAtmosphereEffect.entries.forEach { effect ->
                        FilterChip(
                            selected = effect in settings.effects,
                            onClick = {
                                val updated = settings.effects.toMutableSet().apply {
                                    if (contains(effect)) remove(effect) else add(effect)
                                }
                                onSettingsChange(settings.copy(effects = updated))
                            },
                            label = { Text(effect.title) },
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                Text("Animation intensity", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowAtmosphereIntensity.entries.forEach { option ->
                        FilterChip(
                            selected = settings.intensity == option,
                            onClick = { onSettingsChange(settings.copy(intensity = option)) },
                            label = { Text(option.title) },
                        )
                    }
                }

                Text("Particle density", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowAtmosphereDensity.entries.forEach { option ->
                        FilterChip(
                            selected = settings.density == option,
                            onClick = { onSettingsChange(settings.copy(density = option)) },
                            label = { Text(option.title) },
                        )
                    }
                }

                Text("Audio reactivity", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowAudioSensitivity.entries.forEach { option ->
                        FilterChip(
                            selected = settings.audioSensitivity == option,
                            onClick = { onSettingsChange(settings.copy(audioSensitivity = option)) },
                            label = { Text(option.title) },
                        )
                    }
                }
                Text(
                    "Heartfall continues while Flow is visible, including when playback is paused. Audio Aura reacts only to SYMPHONY playback and needs no microphone permission.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "All Flow atmosphere effects are paused. Your selected effects and intensity are saved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
