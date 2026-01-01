package com.example.healthmon.ble

import android.util.Log
import org.json.JSONObject

/**
 * ESP32'den gelen JSON verisini parse eder
 * 
 * Beklenen format:
 * {"acc":{"x":0.1,"y":0.2,"z":0.98},"gyro":{"x":0.01,"y":0.01,"z":0.01},"ppg":2000}
 * 
 * veya basit format:
 * {"acc":{"x":0.1,"y":0.2,"z":0.98},"ppg":2000}
 */
object SensorDataParser {
    private const val TAG = "SensorDataParser"
    
    fun parse(jsonString: String): SensorData? {
        return try {
            val json = JSONObject(jsonString)
            
            // Accelerometer
            val acc = json.getJSONObject("acc")
            val accX = acc.getDouble("x").toFloat()
            val accY = acc.getDouble("y").toFloat()
            val accZ = acc.getDouble("z").toFloat()
            
            // Gyroscope (optional)
            var gyroX = 0f
            var gyroY = 0f
            var gyroZ = 0f
            if (json.has("gyro")) {
                val gyro = json.getJSONObject("gyro")
                gyroX = gyro.getDouble("x").toFloat()
                gyroY = gyro.getDouble("y").toFloat()
                gyroZ = gyro.getDouble("z").toFloat()
            }
            
            // PPG
            val ppg = json.getInt("ppg")
            
            SensorData(
                accelerometerX = accX,
                accelerometerY = accY,
                accelerometerZ = accZ,
                gyroscopeX = gyroX,
                gyroscopeY = gyroY,
                gyroscopeZ = gyroZ,
                ppg = ppg
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse hatası: ${e.message}")
            null
        }
    }
}
