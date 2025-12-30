package com.example.healthmon.data.service

import com.example.healthmon.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * REST API Service for HealthMon backend
 * Base URL: http://localhost:8000 (or your backend host)
 */
interface ApiService {

    // ===============================================
    // Authentication
    // ===============================================
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // ===============================================
    // Patient APIs (Sending Data)
    // ===============================================
    
    /**
     * Get patient info
     */
    @GET("api/patients/{patientId}")
    suspend fun getPatient(
        @Path("patientId") patientId: String,
        @Header("Authorization") token: String
    ): Response<PatientInfo>

    /**
     * Get patient settings
     */
    @GET("api/patients/{patientId}/settings")
    suspend fun getPatientSettings(
        @Path("patientId") patientId: String,
        @Header("Authorization") token: String
    ): Response<PatientSettings>

    /**
     * Update patient settings
     */
    @PUT("api/patients/{patientId}/settings")
    suspend fun updatePatientSettings(
        @Path("patientId") patientId: String,
        @Body settings: PatientSettingsUpdate,
        @Header("Authorization") token: String
    ): Response<PatientSettings>

    /**
     * Send measurement data (heart rate, inactivity)
     */
    @POST("api/measurements")
    suspend fun sendMeasurement(
        @Body measurement: MeasurementRequest,
        @Header("Authorization") token: String
    ): Response<MeasurementResponse>

    /**
     * Send raw sensor data from ESP32
     */
    @POST("api/sensor-data")
    suspend fun sendSensorData(
        @Body sensorData: SensorDataRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    /**
     * Send ECG segment data
     */
    @POST("api/ecg-segments")
    suspend fun sendEcgSegment(
        @Body ecgSegment: EcgSegmentRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    /**
     * Trigger emergency alert
     */
    @POST("api/emergency")
    suspend fun triggerEmergency(
        @Body emergency: EmergencyRequest,
        @Header("Authorization") token: String
    ): Response<EmergencyLog>

    /**
     * Trigger SOS alert (alias for emergency)
     * Endpoint: POST /api/sos
     */
    @POST("api/sos")
    suspend fun triggerSos(
        @Body emergency: EmergencyRequest,
        @Header("Authorization") token: String
    ): Response<EmergencyLog>

    // ===============================================
    // Caregiver APIs (Receiving Data)
    // ===============================================

    /**
     * Get list of assigned patients for caregiver
     */
    @GET("api/caregivers/{caregiverId}/patients")
    suspend fun getAssignedPatients(
        @Path("caregiverId") caregiverId: String,
        @Header("Authorization") token: String
    ): Response<List<PatientInfo>>

    /**
     * Get caregiver info
     */
    @GET("api/caregivers/{caregiverId}")
    suspend fun getCaregiver(
        @Path("caregiverId") caregiverId: String,
        @Header("Authorization") token: String
    ): Response<CaregiverInfo>

    /**
     * Get measurements for a patient (with pagination)
     */
    @GET("api/patients/{patientId}/measurements")
    suspend fun getPatientMeasurements(
        @Path("patientId") patientId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Header("Authorization") token: String
    ): Response<List<MeasurementResponse>>

    /**
     * Get latest measurement for a patient
     */
    @GET("api/patients/{patientId}/measurements/latest")
    suspend fun getLatestMeasurement(
        @Path("patientId") patientId: String,
        @Header("Authorization") token: String
    ): Response<MeasurementResponse>

    /**
     * Get emergency logs for a patient
     */
    @GET("api/patients/{patientId}/emergency-logs")
    suspend fun getEmergencyLogs(
        @Path("patientId") patientId: String,
        @Query("limit") limit: Int = 20,
        @Header("Authorization") token: String
    ): Response<List<EmergencyLog>>

    /**
     * Resolve an emergency
     */
    @PUT("api/emergency/{emergencyId}/resolve")
    suspend fun resolveEmergency(
        @Path("emergencyId") emergencyId: Long,
        @Header("Authorization") token: String
    ): Response<EmergencyLog>

    /**
     * Get live heart rate view (from v_live_heart_rates DB view)
     */
    @GET("api/live-heart-rates")
    suspend fun getLiveHeartRates(
        @Query("limit") limit: Int = 10,
        @Header("Authorization") token: String
    ): Response<List<MeasurementResponse>>
}
