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
    private val webSocketService: WebSocketService
) {
    companion object {
        private const val TAG = "VitalDataRepository"
    }
    
    // Current mode (true = live backend data, false = mock data)
    private var useLiveData = false
    
    // State flow for vital data
    private val _vitalDataState = MutableStateFlow(VitalDataState(isConnected = false))
    val vitalDataState: StateFlow<VitalDataState> = _vitalDataState.asStateFlow()
    
    /**
     * Connect to backend and start receiving live vital data
     */
    fun connectToBackend(patientId: String, token: String): Flow<VitalDataState> {
        useLiveData = true
        Log.d(TAG, "Connecting to backend for patient: $patientId")
        
        return webSocketService.connectToPatientVitals(patientId, token).map { wsData ->
            val state = VitalDataState(
                heartRate = HeartRateData(bpm = wsData.heartRate),
                inactivity = InactivityData(durationMinutes = wsData.inactivitySeconds / 60),
                isConnected = true
            )
            _vitalDataState.value = state
            state
        }
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
     * Get heart rate data flow (mock mode)
     */
    fun getHeartRateFlow(): Flow<HeartRateData> = mockSensorService.getHeartRateFlow()
    
    /**
     * Get inactivity data flow (mock mode)
     */
    fun getInactivityFlow(): Flow<InactivityData> = mockSensorService.getInactivityFlow().map { minutes ->
        InactivityData(durationMinutes = minutes)
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
