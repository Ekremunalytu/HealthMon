package com.example.healthmon.data.repository

import android.util.Log
import com.example.healthmon.data.model.*
import com.example.healthmon.data.service.ApiService
import com.example.healthmon.data.service.WebSocketService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Caregiver operations
 * Handles receiving data from backend (patient info, measurements, emergencies)
 */
@Singleton
class CaregiverRepository @Inject constructor(
    private val apiService: ApiService,
    private val webSocketService: WebSocketService
) {
    companion object {
        private const val TAG = "CaregiverRepository"
    }

    /**
     * Get caregiver information
     */
    suspend fun getCaregiverInfo(caregiverId: String, token: String): Result<CaregiverInfo> {
        return try {
            val response = apiService.getCaregiver(caregiverId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get caregiver info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting caregiver info", e)
            Result.failure(e)
        }
    }

    /**
     * Get list of assigned patients
     */
    suspend fun getAssignedPatients(caregiverId: String, token: String): Result<List<PatientInfo>> {
        return try {
            val response = apiService.getAssignedPatients(caregiverId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get patients: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting assigned patients", e)
            Result.failure(e)
        }
    }

    /**
     * Get patient measurements with pagination
     */
    suspend fun getPatientMeasurements(
        patientId: String,
        limit: Int = 50,
        offset: Int = 0,
        token: String
    ): Result<List<MeasurementResponse>> {
        return try {
            val response = apiService.getPatientMeasurements(patientId, limit, offset, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get measurements: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting measurements", e)
            Result.failure(e)
        }
    }

    /**
     * Get latest measurement for a patient
     */
    suspend fun getLatestMeasurement(patientId: String, token: String): Result<MeasurementResponse> {
        return try {
            val response = apiService.getLatestMeasurement(patientId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get latest measurement: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest measurement", e)
            Result.failure(e)
        }
    }

    /**
     * Get emergency logs for a patient
     */
    suspend fun getEmergencyLogs(
        patientId: String,
        limit: Int = 20,
        token: String
    ): Result<List<EmergencyLog>> {
        return try {
            val response = apiService.getEmergencyLogs(patientId, limit, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get emergency logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting emergency logs", e)
            Result.failure(e)
        }
    }

    /**
     * Resolve an emergency
     */
    suspend fun resolveEmergency(emergencyId: Long, token: String): Result<EmergencyLog> {
        return try {
            val response = apiService.resolveEmergency(emergencyId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to resolve emergency: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving emergency", e)
            Result.failure(e)
        }
    }

    /**
     * Connect to WebSocket for real-time vital data from a patient
     */
    fun connectToPatientVitals(patientId: String, token: String): Flow<WebSocketVitalData> {
        return webSocketService.connectToPatientVitals(patientId, token)
    }

    /**
     * Get live heart rates (from v_live_heart_rates view)
     */
    suspend fun getLiveHeartRates(limit: Int = 10, token: String): Result<List<MeasurementResponse>> {
        return try {
            val response = apiService.getLiveHeartRates(limit, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get live heart rates: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting live heart rates", e)
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
