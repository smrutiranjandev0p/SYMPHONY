package com.symphony.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.symphony.app.data.local.FlowArtworkStyle

@Composable
internal fun FlowArtworkEffectsCard(
    currentStyle: FlowArtworkStyle,
    onSelectStyle: (FlowArtworkStyle) -> Unit,
    isHighlighted: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isHighlighted) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Flow Artwork Effects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Choose a glass finish for the full-screen song artwork", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                FlowArtworkStyle.entries.forEach { style ->
                    val selected = style == currentStyle
                    val colors = when (style) {
                        FlowArtworkStyle.CRYSTAL -> listOf(Color(0xFF101A2B), Color(0xFF72DFFF), Color(0xFFBDA7FF))
                        FlowArtworkStyle.AURORA -> listOf(Color(0xFF17102C), Color(0xFF9B6BFF), Color(0xFF55D9FF))
                        FlowArtworkStyle.OBSIDIAN -> listOf(Color(0xFF050609), Color(0xFF667085), Color(0xFFE2E8F0))
                    }
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainer)
                            .then(if (selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier)
                            .clickable { onSelectStyle(style) }
                            .padding(7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(colors))
                                .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
                        ) {
                            Box(Modifier.align(Alignment.Center).size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.12f)).border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(8.dp)))
                            if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(style.title, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    }
                }
            }
        }
    }
}
