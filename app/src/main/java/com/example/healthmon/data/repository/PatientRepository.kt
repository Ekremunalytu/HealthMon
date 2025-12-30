package com.example.healthmon.data.repository

import android.util.Log
import com.example.healthmon.data.model.*
import com.example.healthmon.data.service.ApiService
import com.example.healthmon.data.service.IngestionApiService
import com.example.healthmon.data.service.MockSensorService
import com.example.healthmon.data.service.WebSocketService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Patient operations
 * Handles sending data to backend (measurements, emergencies, sensor data)
 * 
 * Uses two backend services:
 * - Core (port 8000): Auth, patient info, settings, measurements
 * - Ingestion (port 8001): Sensor data submission
 */
@Singleton
class PatientRepository @Inject constructor(
    private val apiService: ApiService,
    private val ingestionApiService: IngestionApiService,
    private val webSocketService: WebSocketService
) {
    companion object {
        private const val TAG = "PatientRepository"
    }

    /**
     * Get patient information
     */
    suspend fun getPatientInfo(patientId: String, token: String): Result<PatientInfo> {
        return try {
            val response = apiService.getPatient(patientId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get patient info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting patient info", e)
            Result.failure(e)
        }
    }

    /**
     * Get patient settings
     */
    suspend fun getSettings(patientId: String, token: String): Result<PatientSettings> {
        return try {
            val response = apiService.getPatientSettings(patientId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting settings", e)
            Result.failure(e)
        }
    }

    /**
     * Update patient settings
     */
    suspend fun updateSettings(
        patientId: String,
        settings: PatientSettingsUpdate,
        token: String
    ): Result<PatientSettings> {
        return try {
            val response = apiService.updatePatientSettings(patientId, settings, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating settings", e)
            Result.failure(e)
        }
    }

    /**
     * Send measurement data (heart rate, inactivity) via REST to Core service
     */
    suspend fun sendMeasurement(
        patientId: String,
        heartRate: Int,
        inactivitySeconds: Int,
        token: String
    ): Result<MeasurementResponse> {
        return try {
            val request = MeasurementRequest(
                patientId = patientId,
                heartRate = heartRate,
                inactivitySeconds = inactivitySeconds
            )
            val response = apiService.sendMeasurement(request, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send measurement: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending measurement", e)
            Result.failure(e)
        }
    }

    /**
     * Send measurement data via WebSocket (real-time)
     */
    fun sendMeasurementWebSocket(
        patientId: String,
        heartRate: Int,
        inactivitySeconds: Int,
        status: String
    ): Boolean {
        return webSocketService.sendVitalData(patientId, heartRate, inactivitySeconds, status)
    }

    /**
     * Connect to WebSocket as patient
     */
    fun connectWebSocket(patientId: String, token: String, onConnected: () -> Unit = {}) {
        webSocketService.connectAsPatient(patientId, token, onConnected)
    }

    /**
     * Send raw sensor data to Ingestion service (port 8001)
     * Uses MockSensorService.SensorBatchData format
     * 
     * Endpoint: POST /api/v1/ingest
     */
    suspend fun sendSensorData(
        patientId: String,
        sensorBatch: MockSensorService.SensorBatchData
    ): Result<Unit> {
        return try {
            val request = SensorDataRequest(
                patientId = patientId,
                accelerometer = AccelerometerJson(
                    x = sensorBatch.accelerometerX,
                    y = sensorBatch.accelerometerY,
                    z = sensorBatch.accelerometerZ
                ),
                gyroscope = GyroscopeJson(
                    x = sensorBatch.gyroscopeX,
                    y = sensorBatch.gyroscopeY,
                    z = sensorBatch.gyroscopeZ
                ),
                ppgRaw = sensorBatch.ppgRaw,
                timestamp = sensorBatch.timestamp
            )
            Log.d(TAG, "Sending sensor data to ingestion service: ${request.timestamp}")
            val response = ingestionApiService.ingestSensorData(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Sensor data sent successfully")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to send sensor data: ${response.code()} - ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to send sensor data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending sensor data", e)
            Result.failure(e)
        }
    }

    /**
     * Trigger emergency alert via SOS endpoint
     * Endpoint: POST /api/sos
     */
    suspend fun triggerEmergency(
        patientId: String,
        message: String,
        token: String
    ): Result<EmergencyLog> {
        return try {
            val request = EmergencyRequest(patientId = patientId, message = message)
            // Try SOS endpoint first, fallback to emergency endpoint
            val response = try {
                apiService.triggerSos(request, "Bearer $token")
            } catch (e: Exception) {
                Log.w(TAG, "SOS endpoint failed, trying emergency endpoint", e)
                apiService.triggerEmergency(request, "Bearer $token")
            }
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Emergency/SOS triggered successfully")
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "Failed to trigger emergency: ${response.code()} - ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to trigger emergency: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering emergency", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect WebSocket
     */
    fun disconnectWebSocket() {
        webSocketService.disconnect()
    }
}

