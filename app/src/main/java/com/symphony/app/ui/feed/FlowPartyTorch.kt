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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

/**
 * Optional Flow-only beat-reactive flashlight. Audio waveform access and
 * camera access are requested only after the user enables Party Torch.
 * Pulses are deliberately rate-limited to avoid rapid strobing.
 */
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
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        permissionGranted =
            result[Manifest.permission.CAMERA] == true &&
                result[Manifest.permission.RECORD_AUDIO] == true
        permissionDenied = !permissionGranted
        if (!permissionGranted) onEnabledChange(false)
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
                            runCatching { cameraManager.setTorchMode(flashCameraId, true) }
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
        onClick = {
            if (enabled) {
                onEnabledChange(false)
            } else if (flashCameraId == null) {
                permissionDenied = true
            } else if (permissionGranted) {
                onEnabledChange(true)
            } else {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                )
            }
        },
        modifier = modifier,
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
}
