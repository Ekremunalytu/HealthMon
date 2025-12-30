package com.example.healthmon.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ===============================================
// Authentication Models
// ===============================================

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val role: String,
    @Json(name = "grant_type") val grantType: String = "password"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    // Backend returns both "token" and "access_token", we read both
    val token: String? = null,
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "user_type") val userType: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "patient_id") val patientId: String? = null,
    @Json(name = "caregiver_id") val caregiverId: String? = null
) {
    // Helper to get whichever token is available
    fun getAuthToken(): String? = accessToken ?: token
}

// ===============================================
// Patient Models
// ===============================================

@JsonClass(generateAdapter = true)
data class PatientInfo(
    val id: String,
    val name: String,
    @Json(name = "birth_date") val birthDate: String? = null,
    @Json(name = "medical_info") val medicalInfo: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PatientSettings(
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "bpm_lower_limit") val bpmLowerLimit: Int = 50,
    @Json(name = "bpm_upper_limit") val bpmUpperLimit: Int = 120,
    @Json(name = "max_inactivity_seconds") val maxInactivitySeconds: Int = 900,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PatientSettingsUpdate(
    @Json(name = "bpm_lower_limit") val bpmLowerLimit: Int? = null,
    @Json(name = "bpm_upper_limit") val bpmUpperLimit: Int? = null,
    @Json(name = "max_inactivity_seconds") val maxInactivitySeconds: Int? = null
)

// ===============================================
// Caregiver Models
// ===============================================

@JsonClass(generateAdapter = true)
data class CaregiverInfo(
    val id: String,
    val name: String,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// ===============================================
// Measurement Models (Patient -> Backend)
// ===============================================

@JsonClass(generateAdapter = true)
data class MeasurementRequest(
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "heart_rate") val heartRate: Int,
    @Json(name = "inactivity_seconds") val inactivitySeconds: Int
)

@JsonClass(generateAdapter = true)
data class MeasurementResponse(
    val id: Long,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "heart_rate") val heartRate: Int,
    @Json(name = "inactivity_seconds") val inactivitySeconds: Int,
    val status: String, // NORMAL, WARNING, CRITICAL
    @Json(name = "measured_at") val measuredAt: String
)

// ===============================================
// Sensor Data Queue (Raw sensor data from ESP32)
// ===============================================

@JsonClass(generateAdapter = true)
data class SensorDataRequest(
    @Json(name = "patient_id") val patientId: String,
    val accelerometer: AccelerometerJson,
    val gyroscope: GyroscopeJson,
    @Json(name = "ppg_raw") val ppgRaw: List<Int>,
    val timestamp: Double
)

@JsonClass(generateAdapter = true)
data class AccelerometerJson(
    val x: List<Float>,
    val y: List<Float>,
    val z: List<Float>
)

@JsonClass(generateAdapter = true)
data class GyroscopeJson(
    val x: List<Float>,
    val y: List<Float>,
    val z: List<Float>
)

// ===============================================
// Emergency Models
// ===============================================

@JsonClass(generateAdapter = true)
data class EmergencyRequest(
    @Json(name = "patient_id") val patientId: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class EmergencyLog(
    val id: Long,
    @Json(name = "patient_id") val patientId: String,
    val message: String,
    @Json(name = "is_resolved") val isResolved: Boolean = false,
    @Json(name = "created_at") val createdAt: String
)

// ===============================================
// ECG Segment Models
// ===============================================

@JsonClass(generateAdapter = true)
data class EcgSegmentRequest(
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "sample_rate") val sampleRate: Int = 250,
    @Json(name = "started_at") val startedAt: String,
    @Json(name = "duration_ms") val durationMs: Int,
    val samples: List<Short>
)

// ===============================================
// WebSocket Models
// ===============================================

@JsonClass(generateAdapter = true)
data class WebSocketVitalData(
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "heart_rate") val heartRate: Int,
    @Json(name = "inactivity_seconds") val inactivitySeconds: Int,
    val status: String,
    val timestamp: String
)

// ===============================================
// Generic API Response
// ===============================================

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
