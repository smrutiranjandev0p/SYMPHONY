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
import androidx.compose.material.icons.filled.Layers
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
internal fun FlowArtworkBorderCard(
    currentStyle: FlowArtworkStyle,
    onSelectStyle: (FlowArtworkStyle) -> Unit,
    isHighlighted: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (isHighlighted) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier,
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Artwork Border", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Choose a premium frame for Flow song artwork", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                FlowArtworkStyle.entries.chunked(3).forEach { rowStyles ->
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        rowStyles.forEach { style ->
                            val selected = style == currentStyle
                            val colors = when (style) {
                                FlowArtworkStyle.NONE -> listOf(Color(0xFF343A46), Color(0xFF343A46))
                                FlowArtworkStyle.TITANIUM -> listOf(Color(0xFFE7EDF5), Color(0xFF778397), Color(0xFFF8FAFC))
                                FlowArtworkStyle.CRYSTAL -> listOf(Color(0xFFB9F2FF), Color(0xFF6AC8F2), Color(0xFFE4D9FF))
                                FlowArtworkStyle.AURORA -> listOf(Color(0xFF9C75FF), Color(0xFF54DDF2), Color(0xFFE0B7FF))
                                FlowArtworkStyle.OBSIDIAN -> listOf(Color(0xFF151922), Color(0xFF8D98AA), Color(0xFF303846))
                                FlowArtworkStyle.PRISM -> listOf(Color(0xFF67E8F9), Color(0xFFB58AFF), Color(0xFFFFA6C9))
                            }
                            Column(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainer)
                                    .then(if (selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier)
                                    .clickable { onSelectStyle(style) }.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(11.dp))
                                        .background(Color(0xFF101116)).padding(5.dp)
                                        .border(2.dp, Brush.linearGradient(colors), RoundedCornerShape(8.dp)),
                                ) {
                                    Box(Modifier.align(Alignment.Center).size(17.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.12f)))
                                    if (selected) Icon(Icons.Filled.Check, "Selected", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(14.dp))
                                }
                                Spacer(Modifier.height(7.dp))
                                Text(style.title, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            }
                        }
                        repeat(3 - rowStyles.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
