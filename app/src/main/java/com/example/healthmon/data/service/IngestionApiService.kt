package com.example.healthmon.data.service

import com.example.healthmon.data.model.ApiResponse
import com.example.healthmon.data.model.SensorDataRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Service for Ingestion backend (port 8001)
 * Handles sensor data submission to /api/v1/ingest endpoint
 */
interface IngestionApiService {
    
    /**
     * Send raw sensor data batch to ingestion service
     * Endpoint: POST /api/v1/ingest
     * 
     * Request format:
     * {
     *   "patient_id": "uuid-here",
     *   "timestamp": 1709420000.0,
     *   "accelerometer": {"x": [...], "y": [...], "z": [...]},
     *   "gyroscope": {"x": [...], "y": [...], "z": [...]},
     *   "ppg_raw": [2000, 2050, ...]
     * }
     */
    @POST("api/v1/ingest")
    suspend fun ingestSensorData(
        @Body sensorData: SensorDataRequest
    ): Response<ApiResponse<Unit>>
}
