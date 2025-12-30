package com.example.healthmon.data.model

/**
 * Heart rate data from sensor
 */
data class HeartRateData(
    val bpm: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isNormal: Boolean = bpm in 60..100
)

/**
 * Accelerometer data from sensor
 */
data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate magnitude of movement
     */
    fun magnitude(): Float = kotlin.math.sqrt(x * x + y * y + z * z)
    
    /**
     * Check if there is significant movement
     */
    fun hasMovement(threshold: Float = 1.5f): Boolean = magnitude() > threshold
}

/**
 * Inactivity tracking data
 */
data class InactivityData(
    val durationMinutes: Int,
    val thresholdMinutes: Int = 60,
    val isAlert: Boolean = durationMinutes >= thresholdMinutes
)

/**
 * Combined vital data state for UI
 */
data class VitalDataState(
    val heartRate: HeartRateData = HeartRateData(bpm = 75),
    val inactivity: InactivityData = InactivityData(durationMinutes = 0),
    val isConnected: Boolean = true
)
