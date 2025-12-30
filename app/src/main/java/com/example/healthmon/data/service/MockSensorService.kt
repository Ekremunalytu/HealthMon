package com.example.healthmon.data.service

import com.example.healthmon.data.model.AccelerometerData
import com.example.healthmon.data.model.HeartRateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock sensor service that simulates ESP32 data
 * Generates synthetic sensor data in backend-compatible format
 */
@Singleton
class MockSensorService @Inject constructor() {
    
    private var currentHeartRate = 75
    private var inactivityCounter = 0
    
    companion object {
        // Number of samples per batch (matching ESP32 buffer size)
        private const val SAMPLES_PER_BATCH = 50
        // PPG sample rate ~100Hz means 50 samples = 0.5 seconds
        private const val PPG_SAMPLES_PER_BATCH = 50
    }
    
    /**
     * Emits heart rate data every second with realistic variations
     */
    fun getHeartRateFlow(): Flow<HeartRateData> = flow {
        while (true) {
            // Small variation: ±3 BPM, stay in 60-100 range
            val change = Random.nextInt(-3, 4)
            currentHeartRate = (currentHeartRate + change).coerceIn(60, 100)
            
            emit(HeartRateData(bpm = currentHeartRate))
            delay(1000L)
        }
    }
    
    /**
     * Emits accelerometer data to detect movement
     */
    fun getAccelerometerFlow(): Flow<AccelerometerData> = flow {
        while (true) {
            // Simulate mostly stationary with occasional movement
            val hasMovement = Random.nextFloat() < 0.1f  // 10% chance of movement
            
            val data = if (hasMovement) {
                AccelerometerData(
                    x = Random.nextFloat() * 2 - 1,
                    y = Random.nextFloat() * 2 - 1,
                    z = 9.8f + Random.nextFloat() * 0.5f
                )
            } else {
                AccelerometerData(x = 0.1f, y = 0.1f, z = 9.8f)
            }
            
            emit(data)
            delay(500L)
        }
    }
    
    /**
     * Emits inactivity duration in minutes (increments every 5 seconds for demo)
     */
    fun getInactivityFlow(): Flow<Int> = flow {
        while (true) {
            emit(inactivityCounter)
            delay(5000L)  // 5 seconds for demo (use 60000L for real minutes)
            inactivityCounter++
        }
    }
    
    /**
     * Reset inactivity counter when movement is detected
     */
    fun resetInactivity() {
        inactivityCounter = 0
    }
    
    // =========================================================================
    // Synthetic Sensor Data Generation for Backend API
    // =========================================================================
    
    /**
     * Generate synthetic accelerometer data batch
     * Returns arrays of x, y, z values simulating natural movement patterns
     */
    fun generateAccelerometerBatch(): Triple<List<Float>, List<Float>, List<Float>> {
        val xValues = mutableListOf<Float>()
        val yValues = mutableListOf<Float>()
        val zValues = mutableListOf<Float>()
        
        // Simulate stationary or walking pattern
        val isWalking = Random.nextFloat() < 0.3f  // 30% chance of walking
        
        repeat(SAMPLES_PER_BATCH) { i ->
            if (isWalking) {
                // Walking pattern: sinusoidal with noise
                val phase = (i.toFloat() / SAMPLES_PER_BATCH) * 2 * Math.PI.toFloat()
                xValues.add(kotlin.math.sin(phase) * 0.5f + Random.nextFloat() * 0.1f)
                yValues.add(kotlin.math.cos(phase) * 0.3f + Random.nextFloat() * 0.1f)
                zValues.add(9.8f + kotlin.math.sin(phase * 2) * 0.2f + Random.nextFloat() * 0.05f)
            } else {
                // Stationary with small noise
                xValues.add(Random.nextFloat() * 0.05f - 0.025f)
                yValues.add(Random.nextFloat() * 0.05f - 0.025f)
                zValues.add(9.8f + Random.nextFloat() * 0.02f - 0.01f)
            }
        }
        
        return Triple(xValues, yValues, zValues)
    }
    
    /**
     * Generate synthetic gyroscope data batch
     * Returns arrays of x, y, z angular velocity values (rad/s)
     */
    fun generateGyroscopeBatch(): Triple<List<Float>, List<Float>, List<Float>> {
        val xValues = mutableListOf<Float>()
        val yValues = mutableListOf<Float>()
        val zValues = mutableListOf<Float>()
        
        val hasRotation = Random.nextFloat() < 0.2f  // 20% chance of rotation
        
        repeat(SAMPLES_PER_BATCH) { i ->
            if (hasRotation) {
                // Small rotational movement
                val phase = (i.toFloat() / SAMPLES_PER_BATCH) * Math.PI.toFloat()
                xValues.add(kotlin.math.sin(phase) * 0.1f + Random.nextFloat() * 0.02f)
                yValues.add(kotlin.math.cos(phase) * 0.08f + Random.nextFloat() * 0.02f)
                zValues.add(Random.nextFloat() * 0.05f - 0.025f)
            } else {
                // Nearly stationary
                xValues.add(Random.nextFloat() * 0.01f - 0.005f)
                yValues.add(Random.nextFloat() * 0.01f - 0.005f)
                zValues.add(Random.nextFloat() * 0.01f - 0.005f)
            }
        }
        
        return Triple(xValues, yValues, zValues)
    }
    
    /**
     * Generate synthetic PPG raw data
     * Simulates photoplethysmography signal with realistic cardiac waveform
     */
    fun generatePpgBatch(): List<Int> {
        val ppgValues = mutableListOf<Int>()
        val baseValue = 2000 + Random.nextInt(-100, 100)
        
        // Simulate ~1 heartbeat per 50 samples at 100Hz (~60 BPM)
        repeat(PPG_SAMPLES_PER_BATCH) { i ->
            val phase = (i.toFloat() / PPG_SAMPLES_PER_BATCH) * 2 * Math.PI.toFloat()
            
            // PPG waveform: systolic peak followed by dicrotic notch
            val systolicPeak = if (i in 10..15) 150 else 0
            val dicroticNotch = if (i in 25..30) 50 else 0
            val baseWave = (kotlin.math.sin(phase) * 30).toInt()
            val noise = Random.nextInt(-10, 10)
            
            ppgValues.add(baseValue + systolicPeak + dicroticNotch + baseWave + noise)
        }
        
        return ppgValues
    }
    
    /**
     * Generate complete sensor data packet for backend
     * Returns all sensor data in format ready for SensorDataRequest
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
    
    fun generateSensorBatch(): SensorBatchData {
        val (accX, accY, accZ) = generateAccelerometerBatch()
        val (gyroX, gyroY, gyroZ) = generateGyroscopeBatch()
        val ppg = generatePpgBatch()
        
        return SensorBatchData(
            accelerometerX = accX,
            accelerometerY = accY,
            accelerometerZ = accZ,
            gyroscopeX = gyroX,
            gyroscopeY = gyroY,
            gyroscopeZ = gyroZ,
            ppgRaw = ppg,
            timestamp = System.currentTimeMillis() / 1000.0
        )
    }
}
