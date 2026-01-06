package com.example.healthmon.data.model

/**
 * Common data class for batched sensor data
 * Used by both MockSensorService and RealSensorService (BLE)
 * to pass data to PatientRepository
 */
data class SensorBatchData(
    val accelerometerX: List<Float>,
    val accelerometerY: List<Float>,
    val accelerometerZ: List<Float>,
    val gyroscopeX: List<Float>,
    val gyroscopeY: List<Float>,
    val gyroscopeZ: List<Float>,
    val ppgRaw: List<Int>,
    val timestamp: Double
)
