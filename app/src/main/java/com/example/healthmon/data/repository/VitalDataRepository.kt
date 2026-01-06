package com.example.healthmon.data.repository

import android.util.Log
import com.example.healthmon.data.model.HeartRateData
import com.example.healthmon.data.model.InactivityData
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.model.WebSocketVitalData
import com.example.healthmon.data.service.MockSensorService
import com.example.healthmon.data.service.WebSocketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repository for vital data from sensors
 * 
 * Supports two modes:
 * 1. Mock mode: Uses MockSensorService for local simulation
 * 2. Live mode: Uses WebSocket to receive real data from backend
 */
@Singleton
class VitalDataRepository @Inject constructor(
    private val mockSensorService: MockSensorService,
    private val realSensorService: com.example.healthmon.data.service.RealSensorService,
    private val webSocketService: WebSocketService,
    private val ingestionApiService: com.example.healthmon.data.service.IngestionApiService
) {
    companion object {
        private const val TAG = "VitalDataRepository"
    }
    
    // Current mode (true = live backend data, false = mock data)
    private var useLiveData = false
    
    // BLE buffer
    private var sensorBuffer: com.example.healthmon.ble.SensorDataBuffer? = null
    
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    // State flow for vital data
    private val _vitalDataState = MutableStateFlow(VitalDataState(isConnected = false))
    val vitalDataState: StateFlow<VitalDataState> = _vitalDataState.asStateFlow()
    
    /**
     * Connect to backend and start streaming data
     * This handles both:
     * 1. Receiving data from backend (WebSocket)
     * 2. Sending local BLE sensor data to backend (Ingestion API)
     */
    fun startStreaming(patientId: String, token: String) {
        useLiveData = true
        Log.d(TAG, "Starting streaming for patient: $patientId")
        
        // 1. WebSocket for receiving updates (alerts, processed data)
        webSocketService.connectToPatientVitals(patientId, token) 
        
        // 2. Start BLE scanning if not already connected
        if (!realSensorService.isConnected()) {
            realSensorService.startScan()
        }
        
        // 3. Initialize buffer
        sensorBuffer = com.example.healthmon.ble.SensorDataBuffer(patientId)
        
        // 4. Collect BLE data, buffer it, and send to backend
        repositoryScope.launch {
            realSensorService.getSensorDataFlow().collect { sensorData ->
                // Update local UI state immediately for responsiveness
                val currentState = _vitalDataState.value
                _vitalDataState.value = currentState.copy(
                    heartRate = HeartRateData(bpm = estimateBpm(sensorData.ppg)),
                    isConnected = true
                )
                
                // Buffer and send
                val batch = sensorBuffer?.add(sensorData)
                if (batch != null) {
                    try {
                        Log.d(TAG, "Sending sensor batch to backend")
                        ingestionApiService.ingestSensorData(batch)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send sensor data: ${e.message}")
                    }
                }
            }
        }
    }
    
    private fun estimateBpm(ppg: Int): Int {
        // Simple placeholder for UI feedback
        return 60 + ((ppg - 1800) / 10).coerceIn(0, 100)
    }

    /**
     * Connect to backend and start receiving live vital data (Simplified version for backward compatibility)
     */
    fun connectToBackend(patientId: String, token: String): Flow<VitalDataState> {
        startStreaming(patientId, token)
        return vitalDataState
    }
    
    /**
     * Update vital data state from WebSocket message
     */
    fun updateFromWebSocket(wsData: WebSocketVitalData) {
        _vitalDataState.value = VitalDataState(
            heartRate = HeartRateData(bpm = wsData.heartRate),
            inactivity = InactivityData(durationMinutes = wsData.inactivitySeconds / 60),
            isConnected = true
        )
    }
    
    /**
     * Get heart rate data flow
     */
    fun getHeartRateFlow(): Flow<HeartRateData> {
        return if (useLiveData) {
             realSensorService.getHeartRateFlow()
        } else {
             mockSensorService.getHeartRateFlow()
        }
    }
    
    /**
     * Get inactivity data flow
     */
    fun getInactivityFlow(): Flow<InactivityData> {
         return if (useLiveData) {
             // In live mode, inactivity usually comes from backend WebSocket
             // But we can return a placeholder or map from realSensorService if needed
             kotlinx.coroutines.flow.flowOf(InactivityData(0)) 
         } else {
             mockSensorService.getInactivityFlow().map { minutes ->
                InactivityData(durationMinutes = minutes)
             }
         }
    }
    
    /**
     * Get combined vital data state flow
     * Returns live data if connected, otherwise mock data
     */
    fun getVitalDataStateFlow(): StateFlow<VitalDataState> = _vitalDataState.asStateFlow()
    
    /**
     * Start mock data generation (for testing without backend)
     */
    suspend fun startMockData() {
        useLiveData = false
        Log.d(TAG, "Starting mock data generation")
        
        mockSensorService.getHeartRateFlow().collect { heartRate ->
            val currentState = _vitalDataState.value
            _vitalDataState.value = currentState.copy(
                heartRate = heartRate,
                isConnected = true
            )
        }
    }
    
    /**
     * Reset inactivity tracking
     */
    fun resetInactivity() {
        if (!useLiveData) {
            mockSensorService.resetInactivity()
        }
        // For live mode, inactivity is tracked by backend
    }
    
    /**
     * Set connection status
     */
    fun setConnected(connected: Boolean) {
        _vitalDataState.value = _vitalDataState.value.copy(isConnected = connected)
    }
    
    /**
     * Check if using live backend data
     */
    fun isUsingLiveData(): Boolean = useLiveData
}

