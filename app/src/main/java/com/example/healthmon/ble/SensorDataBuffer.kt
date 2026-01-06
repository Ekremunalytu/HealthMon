package com.example.healthmon.ble

import com.example.healthmon.data.model.AccelerometerJson
import com.example.healthmon.data.model.GyroscopeJson
import com.example.healthmon.data.model.SensorDataRequest

/**
 * Buffers single sensor data points into a batch for backend ingestion.
 * Typically accumulates 1 second worth of data (e.g. 5 samples at 5Hz).
 */
class SensorDataBuffer(
    private val patientId: String,
    private val bufferSize: Int = 5 // 5 samples @ 5Hz = 1 second
) {
    private val accX = ArrayList<Float>()
    private val accY = ArrayList<Float>()
    private val accZ = ArrayList<Float>()
    
    private val gyroX = ArrayList<Float>()
    private val gyroY = ArrayList<Float>()
    private val gyroZ = ArrayList<Float>()
    
    private val ppg = ArrayList<Int>()
    
    private var startTime: Long = 0
    
    fun add(data: SensorData): SensorDataRequest? {
        if (startTime == 0L) {
            startTime = data.timestamp
        }
        
        accX.add(data.accelerometerX)
        accY.add(data.accelerometerY)
        accZ.add(data.accelerometerZ)
        
        gyroX.add(data.gyroscopeX)
        gyroY.add(data.gyroscopeY)
        gyroZ.add(data.gyroscopeZ)
        
        ppg.add(data.ppg)
        
        if (accX.size >= bufferSize) {
            return flush()
        }
        
        return null
    }
    
    private fun flush(): SensorDataRequest {
        val request = SensorDataRequest(
            patientId = patientId,
            timestamp = startTime / 1000.0, // Convert ms to seconds
            accelerometer = AccelerometerJson(
                x = ArrayList(accX),
                y = ArrayList(accY),
                z = ArrayList(accZ)
            ),
            gyroscope = GyroscopeJson(
                x = ArrayList(gyroX),
                y = ArrayList(gyroY),
                z = ArrayList(gyroZ)
            ),
            ppgRaw = ArrayList(ppg)
        )
        
        clear()
        return request
    }
    
    private fun clear() {
        accX.clear()
        accY.clear()
        accZ.clear()
        gyroX.clear()
        gyroY.clear()
        gyroZ.clear()
        ppg.clear()
        startTime = 0
    }
}
