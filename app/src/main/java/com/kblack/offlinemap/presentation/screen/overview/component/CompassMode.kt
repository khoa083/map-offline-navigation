package com.kblack.offlinemap.presentation.screen.overview.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Returns the current compass heading in degrees [0, 360), or **null** when:
 * - No suitable sensor is available on the device
 * - Sensor accuracy is SENSOR_STATUS_UNRELIABLE (e.g. magnetic interference)
 *
 * Sensor priority:
 * 1. TYPE_ROTATION_VECTOR  – fused (accel + gyro + magnetometer). Best accuracy & stability.
 * 2. TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD – legacy hardware fallback.
 *    Note: TYPE_GAME_ROTATION_VECTOR is intentionally NOT used because it ignores
 *    magnetic north and is unsuitable for compass functionality.
 *
 * All sensor values are correctly remapped to account for the current display rotation,
 * so the heading is accurate regardless of screen orientation.
 *
 * [updateThresholdDeg] changes smaller than this value are suppressed to avoid
 * unnecessary recompositions.
 * [smoothingAlpha] Low-pass filter coefficient in [0, 1]. Lower value = smoother
 * but more latency. 0.15 is a sensible default.
 * [sensorDelay] One of SensorManager.SENSOR_DELAY_* constants.
 */
//todo: Create by Claude 4.6
//https://proandroiddev.com/update-for-your-compass-new-android-orientation-api-dc4e5c25ca35
@Composable
fun rememberCompassMode(
    updateThresholdDeg: Float = 1f,
    smoothingAlpha: Float = 0.3f,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
): State<Float?> {
    val context = LocalContext.current
    val heading = remember { mutableStateOf<Float?>(null) }

    // rememberUpdatedState: the listener always reads the latest parameter values
    // without needing to unregister/re-register sensors on every recomposition.
    val thresholdState = rememberUpdatedState(updateThresholdDeg)
    val alphaState = rememberUpdatedState(smoothingAlpha)

    // Only re-run the effect (and re-register sensors) when context or sensorDelay changes.
    // Changes to threshold/alpha are handled by rememberUpdatedState above.
    DisposableEffect(context, sensorDelay) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
            ?: return@DisposableEffect onDispose { }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // ── Primary path: TYPE_ROTATION_VECTOR ──────────────────────────────────
        if (rotationSensor != null) {
            val listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val adjustedMatrix = FloatArray(9)
                private val orientation = FloatArray(3)
                private var smoothedHeading: Float? = null

                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    if (!remapForDisplay(context, rotationMatrix, adjustedMatrix)) return
                    SensorManager.getOrientation(adjustedMatrix, orientation)
                    smoothedHeading = applySmoothing(
                        azimuthRad = orientation[0],
                        smoothed = smoothedHeading,
                        alpha = alphaState.value,
                        threshold = thresholdState.value,
                        heading = heading,
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                        heading.value = null
                        smoothedHeading = null
                    }
                }
            }
            sensorManager.registerListener(listener, rotationSensor, sensorDelay)
            return@DisposableEffect onDispose { sensorManager.unregisterListener(listener) }
        }

        // ── Fallback path: Accelerometer + Magnetometer ──────────────────────────
        // TYPE_GAME_ROTATION_VECTOR is intentionally skipped — it has no magnetic north
        // and would produce random/incorrect compass readings.
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer == null || magnetometer == null) {
            return@DisposableEffect onDispose { }
        }

        val fallbackListener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val adjustedMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            private var gravity: FloatArray? = null
            private var geomagnetic: FloatArray? = null
            private var smoothedHeading: Float? = null
            private var accelReliable = true
            private var magReliable = true

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                    Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
                    else -> return
                }

                val g = gravity ?: return
                val m = geomagnetic ?: return
                if (!accelReliable || !magReliable) return

                // Returns false during free fall or near-zero gravity — skip the frame.
                if (!SensorManager.getRotationMatrix(rotationMatrix, null, g, m)) return

                if (!remapForDisplay(context, rotationMatrix, adjustedMatrix)) return
                SensorManager.getOrientation(adjustedMatrix, orientation)
                smoothedHeading = applySmoothing(
                    azimuthRad = orientation[0],
                    smoothed = smoothedHeading,
                    alpha = alphaState.value,
                    threshold = thresholdState.value,
                    heading = heading,
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                val reliable = accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE
                when (sensor?.type) {
                    Sensor.TYPE_ACCELEROMETER -> accelReliable = reliable
                    Sensor.TYPE_MAGNETIC_FIELD -> magReliable = reliable
                }
                if (!accelReliable || !magReliable) {
                    heading.value = null
                    smoothedHeading = null
                }
            }
        }

        sensorManager.registerListener(fallbackListener, accelerometer, sensorDelay)
        sensorManager.registerListener(fallbackListener, magnetometer, sensorDelay)
        onDispose { sensorManager.unregisterListener(fallbackListener) }
    }

    return heading
}

// ── Private helpers ──────────────────────────────────────────────────────────

/**
 * Applies exponential smoothing over the shortest angular path and updates
 * [heading] state only when the change exceeds [threshold].
 *
 * Returns the new smoothedHeading value so the caller can store it.
 */
private fun applySmoothing(
    azimuthRad: Float,
    smoothed: Float?,
    alpha: Float,
    threshold: Float,
    heading: MutableState<Float?>,
): Float {
    val normalized = normalizeDegree(Math.toDegrees(azimuthRad.toDouble()).toFloat())
    val newSmoothed = smoothed?.let { prev ->
        normalizeDegree(prev + shortestAngleDelta(prev, normalized) * alpha)
    } ?: normalized

    val current = heading.value
    if (current == null || abs(shortestAngleDelta(current, newSmoothed)) >= threshold) {
        heading.value = newSmoothed
    }
    return newSmoothed
}

/**
 * Remaps [rotationMatrix] into [adjustedMatrix] to account for the current
 * display rotation. Returns false if remapping fails (caller should skip the frame).
 */
private fun remapForDisplay(
    context: Context,
    rotationMatrix: FloatArray,
    adjustedMatrix: FloatArray,
): Boolean {
    return when (getDisplayRotation(context)) {
        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
            adjustedMatrix,
        )
        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
            adjustedMatrix,
        )
        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
            adjustedMatrix,
        )
        else -> {
            // ROTATION_0: no remapping needed
            rotationMatrix.copyInto(adjustedMatrix)
            true
        }
    }
}

/**
 * Returns the current display rotation using the non-deprecated API on API 30+
 * and the legacy API on older versions.
 */
private fun getDisplayRotation(context: Context): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // context.display is null only for non-visual contexts (e.g. Service).
        // In a Composable, LocalContext is always an Activity, so this is safe.
        context.display?.rotation ?: Surface.ROTATION_0
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(WindowManager::class.java)
            ?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }
}

/** Maps any angle into [0, 360). */
fun normalizeDegree(value: Float): Float = ((value % 360f) + 360f) % 360f

/**
 * Returns the shortest signed angle delta from [fromDeg] to [toDeg] in (-180, 180].
 * The `+540 % 360 - 180` formula already guarantees this range; no extra clamp needed.
 */
fun shortestAngleDelta(fromDeg: Float, toDeg: Float): Float =
    ((toDeg - fromDeg + 540f) % 360f) - 180f