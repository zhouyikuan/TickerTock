package com.cs407.tickertock.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Composable that detects phone tilt gestures using the accelerometer
 * Tilt right = swipe right (include article)
 * Tilt left = swipe left (skip article)
 */
@Composable
fun TiltDetector(
    enabled: Boolean,
    onTiltRight: () -> Unit,
    onTiltLeft: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(enabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastTriggerTime = 0L
        val cooldownMillis = 1000L // 1 second cooldown between tilts
        val tiltThreshold = 3.5f // ~35 degree tilt threshold

        var tiltStartTime = 0L
        var currentTiltDirection: TiltDirection? = null
        val debounceMillis = 200L // Require stable tilt for 200ms

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val x = event.values[0] // X axis acceleration
                val currentTime = System.currentTimeMillis()

                // Check if we're in cooldown period
                if (currentTime - lastTriggerTime < cooldownMillis) {
                    return
                }

                // Determine tilt direction
                // tilting right = neg x , tilting left = pos x
                val detectedTilt = when {
                    x > tiltThreshold -> TiltDirection.LEFT
                    x < -tiltThreshold -> TiltDirection.RIGHT
                    else -> null
                }

                // Debouncing: Needs tilt direction to remain same
                if (detectedTilt != null) {
                    if (currentTiltDirection == detectedTilt) {
                        // if tilt direction is same for debounceMillis time,
                        // do action and reset.
                        if (currentTime - tiltStartTime >= debounceMillis) {
                            // Trigger the action
                            when (detectedTilt) {
                                TiltDirection.RIGHT -> onTiltRight()
                                TiltDirection.LEFT -> onTiltLeft()
                            }
                            lastTriggerTime = currentTime
                            currentTiltDirection = null // Reset
                            tiltStartTime = 0L
                        } else{
                            // Do nothing
                        }
                    } else {
                        // New direction detected, update vars
                        currentTiltDirection = detectedTilt
                        tiltStartTime = currentTime
                    }
                } else {
                    // No tilt => make vars null
                    currentTiltDirection = null
                    tiltStartTime = 0L
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Inherited method, not needed.
            }
        }

        // if enabled, enable sensor.
        if (enabled) {
            accelerometer?.let {
                sensorManager.registerListener(
                    sensorEventListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
}

private enum class TiltDirection {
    LEFT, RIGHT
}
