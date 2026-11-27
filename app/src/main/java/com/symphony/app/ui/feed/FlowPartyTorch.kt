package com.symphony.app.ui.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicBoolean
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

/**
 * Flow-only, audio-reactive flashlight. Audio waveform is read from the
 * active player session, not from the microphone. Pulses are short and limited.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val preferences = remember(context) {
        context.getSharedPreferences("flow_party_torch", Context.MODE_PRIVATE)
    }
    val flashCameraId = remember(cameraManager) {
        runCatching {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }
    val maxStrength = remember(cameraManager, flashCameraId) {
        if (Build.VERSION.SDK_INT >= 33 && flashCameraId != null) {
            runCatching {
                cameraManager.getCameraCharacteristics(flashCameraId)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
            }.getOrDefault(1).coerceAtLeast(1)
        } else 1
    }
    val adjustableFlash = Build.VERSION.SDK_INT >= 33 && maxStrength > 1
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }
    var showLightStudio by remember { mutableStateOf(false) }
    var visualizerReady by remember { mutableStateOf(false) }
    var pulseCount by remember { mutableIntStateOf(0) }
    var torchError by remember { mutableStateOf(false) }
    var strength by remember(maxStrength) {
        mutableIntStateOf(preferences.getInt("strength", 1).coerceIn(1, maxStrength))
    }
    var sensitivity by remember {
        mutableIntStateOf(preferences.getInt("sensitivity", 64).coerceIn(18, 80))
    }
    val latestStrength by rememberUpdatedState(strength)
    val latestSensitivity by rememberUpdatedState(sensitivity)

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
        visualizerReady = false
        torchError = false
        if (!enabled || !isPlaying || !permissionGranted || audioSessionId <= 0 || flashCameraId == null) {
            flashCameraId?.let { id -> runCatching { cameraManager.setTorchMode(id, false) } }
            onDispose { }
        } else {
            val effectActive = AtomicBoolean(true)
            var lastPulseAt = 0L
            var torchOn = false
            var pulseTask: Runnable? = null
            var releaseTask: Runnable? = null
            val visualizer = runCatching { Visualizer(audioSessionId) }.getOrNull()
            val listener = object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
                    if (waveform.isEmpty()) return
                    var sum = 0.0
                    for (sample in waveform) {
                        val centered = (sample.toInt() and 0xFF) - 128
                        sum += centered * centered
                    }
                    val rms = sqrt(sum / waveform.size)
                    val now = SystemClock.elapsedRealtime()
                    val pulseThreshold = (98 - latestSensitivity).coerceIn(18, 80)\n                    if (effectActive.get() && rms >= pulseThreshold && now - lastPulseAt >= 420L) {
                        lastPulseAt = now
                        val pulse = Runnable {
                            if (!effectActive.get()) return@Runnable
                            releaseTask?.let(mainHandler::removeCallbacks)
                            val turnedOn = runCatching {
                                if (adjustableFlash) {
                                    cameraManager.turnOnTorchWithStrengthLevel(flashCameraId, latestStrength.coerceIn(1, maxStrength))
                                } else {
                                    cameraManager.setTorchMode(flashCameraId, true)
                                }
                            }.isSuccess
                            if (turnedOn && effectActive.get()) {
                                torchOn = true
                                pulseCount += 1
                                releaseTask = Runnable {
                                    runCatching { cameraManager.setTorchMode(flashCameraId, false) }
                                    torchOn = false
                                }.also { mainHandler.postDelayed(it, 110L) }
                            } else {
                                runCatching { cameraManager.setTorchMode(flashCameraId, false) }
                                torchError = true
                            }
                        }
                        pulseTask = pulse
                        mainHandler.post(pulse)
                    }
                }

                override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) = Unit
            }
            val started = runCatching {
                visualizer?.captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
                visualizer?.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate() / 2, true, false)
                visualizer?.enabled = true
                visualizer != null && visualizer.enabled
            }.getOrDefault(false)
            visualizerReady = started
            if (!started) runCatching { visualizer?.release() }

            onDispose {
                effectActive.set(false)
                pulseTask?.let(mainHandler::removeCallbacks)
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
                    permissionDenied -> "Torch permission needed"
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
                if (adjustableFlash) {
                    Text("Level $strength of $maxStrength", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = strength.toFloat(),
                        onValueChange = { value ->
                            strength = value.toInt().coerceIn(1, maxStrength)
                            preferences.edit().putInt("strength", strength).apply()
                        },
                        valueRange = 1f..maxStrength.toFloat(),
                        steps = (maxStrength - 2).coerceAtLeast(0),
                    )
                    Text("Selected level is applied to each music-reactive pulse.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        "Your phone exposes only standard torch ON/OFF control. Adjustable flash intensity is not supported, so no brightness slider is shown.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("Music sync sensitivity", style = MaterialTheme.typography.titleMedium)
                val analyserStatus = when {
                    !enabled -> "Turn Party Torch on to activate"
                    !isPlaying -> "Waiting for playback"
                    audioSessionId <= 0 -> "Waiting for audio session"
                    visualizerReady -> "Audio analyser active"
                    else -> "Audio analyser unavailable"
                }
                Text("Sensitivity $sensitivity · $analyserStatus", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)\n                Text("Higher values react to quieter parts of the music.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = sensitivity.toFloat(),
                    onValueChange = { value ->
                        sensitivity = value.toInt().coerceIn(18, 80)
                        preferences.edit().putInt("sensitivity", sensitivity).apply()
                    },
                    valueRange = 18f..80f,
                    steps = 11,
                )
                Text(
                    if (pulseCount > 0) "Detected flash pulses this session: $pulseCount" else "Play music while Party Torch is on to test sync. Pulses are short and rate-limited.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (torchError) {
                    Text("The camera flash could not be controlled. Another camera app may be using it.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text("Colour temperature / RGB", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Standard Android flashlight APIs cannot change physical flash colour. No unsupported colour control is offered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Safety: no rapid strobe mode. Pulses are limited to one every 420 ms and switch off automatically.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
