package com.symphony.app.ui.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.os.Build
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

/**
 * Optional Flow-only beat-reactive flashlight. Audio waveform access and
 * camera access are requested only after the user enables Party Torch.
 * Pulses are deliberately rate-limited to avoid rapid strobing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FlowPartyTorch(
    enabled: Boolean,
    isPlaying: Boolean,
    audioSessionId: Int,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraManager = remember(context) {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    val flashCameraId = remember(cameraManager) {
        runCatching {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }
    var showLightStudio by remember { mutableStateOf(false) }
    val maxStrength = remember(cameraManager, flashCameraId) {
        if (Build.VERSION.SDK_INT >= 33 && flashCameraId != null) {
            runCatching {
                cameraManager.getCameraCharacteristics(flashCameraId)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
            }.getOrDefault(1).coerceAtLeast(1)
        } else 1
    }
    var strength by remember(maxStrength) { mutableStateOf(maxStrength) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        permissionGranted =
            result[Manifest.permission.CAMERA] == true &&
                result[Manifest.permission.RECORD_AUDIO] == true
        permissionDenied = !permissionGranted
        onEnabledChange(permissionGranted)
    }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(enabled, isPlaying, audioSessionId, permissionGranted, flashCameraId) {
        if (!enabled || !isPlaying || !permissionGranted || audioSessionId <= 0 || flashCameraId == null) {
            flashCameraId?.let { id -> runCatching { cameraManager.setTorchMode(id, false) } }
            onDispose { }
        } else {
            var lastPulseAt = 0L
            var torchOn = false
            var releaseTask: Runnable? = null
            val visualizer = runCatching { Visualizer(audioSessionId) }.getOrNull()
            val listener = object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    waveform: ByteArray,
                    samplingRate: Int,
                ) {
                    if (waveform.isEmpty()) return
                    var sum = 0.0
                    for (sample in waveform) {
                        val centered = (sample.toInt() and 0xFF) - 128
                        sum += centered * centered
                    }
                    val rms = sqrt(sum / waveform.size)
                    val now = android.os.SystemClock.elapsedRealtime()
                    // A short, bounded transient detector. The cooldown caps
                    // pulses to about 3 per second even on dense material.
                    if (rms >= 34.0 && now - lastPulseAt >= 320L) {
                        lastPulseAt = now
                        mainHandler.post {
                            releaseTask?.let(mainHandler::removeCallbacks)
                            runCatching {
                                if (Build.VERSION.SDK_INT >= 33 && maxStrength > 1) {
                                    cameraManager.turnOnTorchWithStrengthLevel(flashCameraId, strength.coerceIn(1, maxStrength))
                                } else cameraManager.setTorchMode(flashCameraId, true)
                            }
                            torchOn = true
                            releaseTask = Runnable {
                                runCatching { cameraManager.setTorchMode(flashCameraId, false) }
                                torchOn = false
                            }.also { mainHandler.postDelayed(it, 95L) }
                        }
                    }
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    fft: ByteArray,
                    samplingRate: Int,
                ) = Unit
            }
            runCatching {
                visualizer?.captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
                visualizer?.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate() / 2, true, false)
                visualizer?.enabled = true
            }
            onDispose {
                releaseTask?.let(mainHandler::removeCallbacks)
                if (torchOn) runCatching { cameraManager.setTorchMode(flashCameraId, false) }
                runCatching { visualizer?.enabled = false }
                runCatching { visualizer?.release() }
                runCatching { cameraManager.setTorchMode(flashCameraId, false) }
            }
        }
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        modifier = modifier.combinedClickable(
            onClick = {
                if (enabled) onEnabledChange(false)
                else if (flashCameraId == null) permissionDenied = true
                else if (permissionGranted) onEnabledChange(true)
                else permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            },
            onLongClick = { showLightStudio = true },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (enabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                contentDescription = if (enabled) "Turn Party Torch off" else "Turn Party Torch on",
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = when {
                    flashCameraId == null -> "No flash"
                    permissionDenied -> "Torch unavailable"
                    enabled && isPlaying -> "Party Torch"
                    enabled -> "Torch paused"
                    else -> "Party Torch"
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

    if (showLightStudio) {
        ModalBottomSheet(onDismissRequest = { showLightStudio = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Party Torch · Light Studio", style = MaterialTheme.typography.titleLarge)
                Text("Flash intensity", style = MaterialTheme.typography.titleMedium)
                if (Build.VERSION.SDK_INT >= 33 && maxStrength > 1) {
                    Text("Level $strength of $maxStrength", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = strength.toFloat(),
                        onValueChange = { value ->
                            strength = value.toInt().coerceIn(1, maxStrength)
                            if (enabled && isPlaying) runCatching {
                                cameraManager.turnOnTorchWithStrengthLevel(flashCameraId!!, strength)
                            }
                        },
                        valueRange = 1f..maxStrength.toFloat(),
                        steps = (maxStrength - 2).coerceAtLeast(0),
                    )
                } else {
                    Text(
                        "This phone does not expose adjustable flash intensity. Standard ON/OFF flash will be used.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("Music sync", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Beat-reactive pulses remain enabled while Party Torch is active. Pulses are rate-limited and stop when playback pauses or you leave Flow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Colour temperature / RGB", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Standard Android flash APIs cannot change physical flash colour. Device-specific controls will only be shown when a supported interface is verified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Safety: no rapid strobe mode. Long-press Party Torch to reopen these settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }}
