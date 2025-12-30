package com.example.healthmon.ui.patient

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.local.TokenManager
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.PatientRepository
import com.example.healthmon.data.repository.VitalDataRepository
import com.example.healthmon.data.service.MockSensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Patient screen
 * Handles data streaming to backend via REST API and WebSocket
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val vitalDataRepository: VitalDataRepository,
    private val patientRepository: PatientRepository,
    private val mockSensorService: MockSensorService,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val TAG = "PatientViewModel"
        
        // Data sending intervals (milliseconds)
        private const val WEBSOCKET_INTERVAL_MS = 1000L      // 1 second - real-time
        private const val MEASUREMENT_INTERVAL_MS = 10000L   // 10 seconds - REST
        private const val SENSOR_DATA_INTERVAL_MS = 5000L    // 5 seconds - REST
    }

    // Data streaming jobs
    private var webSocketStreamingJob: Job? = null
    private var measurementStreamingJob: Job? = null
    private var sensorDataStreamingJob: Job? = null

    // Data transmission state
    private val _dataTransmissionState = MutableStateFlow(DataTransmissionState())
    val dataTransmissionState: StateFlow<DataTransmissionState> = _dataTransmissionState.asStateFlow()

    /**
     * Vital data state flow for UI observation
     */
    val vitalDataState: StateFlow<VitalDataState> = vitalDataRepository
        .getVitalDataStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VitalDataState()
        )

    /**
     * Start all data streaming to backend
     * Should be called after successful login with valid patientId and token
     */
    fun startDataStreaming(patientId: String, token: String) {
        Log.d(TAG, "Starting data streaming for patient: $patientId")
        
        // Connect to WebSocket first
        patientRepository.connectWebSocket(patientId, token) {
            Log.d(TAG, "WebSocket connected, starting data loops")
            _dataTransmissionState.value = _dataTransmissionState.value.copy(
                isWebSocketConnected = true
            )
        }

        // Start WebSocket real-time streaming (every 1 second)
        startWebSocketStreaming(patientId)

        // Start REST API measurement streaming (every 10 seconds)
        startMeasurementStreaming(patientId, token)

        // Start REST API sensor data streaming (every 5 seconds)
        startSensorDataStreaming(patientId)
    }

    /**
     * WebSocket streaming - sends vital data every second for real-time updates
     */
    private fun startWebSocketStreaming(patientId: String) {
        webSocketStreamingJob?.cancel()
        webSocketStreamingJob = viewModelScope.launch {
            Log.d(TAG, "WebSocket streaming started")
            while (isActive) {
                try {
                    val currentState = vitalDataState.value
                    val status = determineStatus(currentState.heartRate.bpm, currentState.inactivity.durationMinutes)
                    
                    val success = patientRepository.sendMeasurementWebSocket(
                        patientId = patientId,
                        heartRate = currentState.heartRate.bpm,
                        inactivitySeconds = currentState.inactivity.durationMinutes * 60,
                        status = status
                    )
                    
                    if (success) {
                        _dataTransmissionState.value = _dataTransmissionState.value.copy(
                            lastWebSocketSendTime = System.currentTimeMillis(),
                            webSocketSendCount = _dataTransmissionState.value.webSocketSendCount + 1
                        )
                        Log.d(TAG, "WebSocket: Sent HR=${currentState.heartRate.bpm}, Inactivity=${currentState.inactivity.durationMinutes}min")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "WebSocket streaming error", e)
                }
                delay(WEBSOCKET_INTERVAL_MS)
            }
        }
    }

    /**
     * REST measurement streaming - sends processed measurement every 10 seconds
     * Endpoint: POST /api/measurements
     */
    private fun startMeasurementStreaming(patientId: String, token: String) {
        measurementStreamingJob?.cancel()
        measurementStreamingJob = viewModelScope.launch {
            Log.d(TAG, "Measurement streaming started")
            while (isActive) {
                try {
                    val currentState = vitalDataState.value
                    
                    val result = patientRepository.sendMeasurement(
                        patientId = patientId,
                        heartRate = currentState.heartRate.bpm,
                        inactivitySeconds = currentState.inactivity.durationMinutes * 60,
                        token = token
                    )
                    
                    result.fold(
                        onSuccess = { response ->
                            _dataTransmissionState.value = _dataTransmissionState.value.copy(
                                lastMeasurementSendTime = System.currentTimeMillis(),
                                measurementSendCount = _dataTransmissionState.value.measurementSendCount + 1,
                                lastMeasurementStatus = response.status
                            )
                            Log.d(TAG, "REST Measurement: Sent successfully, status=${response.status}")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "REST Measurement: Failed to send", error)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Measurement streaming error", e)
                }
                delay(MEASUREMENT_INTERVAL_MS)
            }
        }
    }

    /**
     * REST sensor data streaming - sends raw sensor batches every 5 seconds
     * Endpoint: POST /api/sensor-data (via ingestion service)
     */
    private fun startSensorDataStreaming(patientId: String) {
        sensorDataStreamingJob?.cancel()
        sensorDataStreamingJob = viewModelScope.launch {
            Log.d(TAG, "Sensor data streaming started")
            while (isActive) {
                try {
                    // Generate synthetic sensor batch from MockSensorService
                    val sensorBatch = mockSensorService.generateSensorBatch()
                    
                    val result = patientRepository.sendSensorData(
                        patientId = patientId,
                        sensorBatch = sensorBatch
                    )
                    
                    result.fold(
                        onSuccess = {
                            _dataTransmissionState.value = _dataTransmissionState.value.copy(
                                lastSensorDataSendTime = System.currentTimeMillis(),
                                sensorDataSendCount = _dataTransmissionState.value.sensorDataSendCount + 1
                            )
                            Log.d(TAG, "REST Sensor Data: Sent batch at timestamp=${sensorBatch.timestamp}")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "REST Sensor Data: Failed to send", error)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Sensor data streaming error", e)
                }
                delay(SENSOR_DATA_INTERVAL_MS)
            }
        }
    }

    /**
     * Send emergency alert to backend
     * Endpoint: POST /api/sos
     */
    fun sendEmergency(patientId: String, message: String, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "Sending emergency: $message")
            
            val result = patientRepository.triggerEmergency(
                patientId = patientId,
                message = message,
                token = token
            )
            
            result.fold(
                onSuccess = { emergencyLog ->
                    Log.d(TAG, "Emergency sent successfully, id=${emergencyLog.id}")
                    _dataTransmissionState.value = _dataTransmissionState.value.copy(
                        lastEmergencySendTime = System.currentTimeMillis()
                    )
                    onResult(true)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to send emergency", error)
                    onResult(false)
                }
            )
        }
    }

    /**
     * Determine status based on heart rate and inactivity
     */
    private fun determineStatus(heartRate: Int, inactivityMinutes: Int): String {
        return when {
            heartRate < 50 || heartRate > 120 -> "CRITICAL"
            heartRate < 60 || heartRate > 100 -> "WARNING"
            inactivityMinutes > 60 -> "WARNING"
            else -> "NORMAL"
        }
    }

    /**
     * Stop all data streaming
     */
    fun stopDataStreaming() {
        Log.d(TAG, "Stopping all data streaming")
        webSocketStreamingJob?.cancel()
        measurementStreamingJob?.cancel()
        sensorDataStreamingJob?.cancel()
        patientRepository.disconnectWebSocket()
        
        _dataTransmissionState.value = _dataTransmissionState.value.copy(
            isWebSocketConnected = false
        )
    }

    /**
     * Logout user
     */
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            stopDataStreaming()
            tokenManager.clearAuthData()
            onLogout()
        }
    }

    /**
     * Reset inactivity counter
     */
    fun resetInactivity() {
        vitalDataRepository.resetInactivity()
    }

    override fun onCleared() {
        super.onCleared()
        stopDataStreaming()
    }
}

/**
 * Data transmission state for UI
 */
data class DataTransmissionState(
    val isWebSocketConnected: Boolean = false,
    val lastWebSocketSendTime: Long = 0,
    val lastMeasurementSendTime: Long = 0,
    val lastSensorDataSendTime: Long = 0,
    val lastEmergencySendTime: Long = 0,
    val webSocketSendCount: Int = 0,
    val measurementSendCount: Int = 0,
    val sensorDataSendCount: Int = 0,
    val lastMeasurementStatus: String = ""
)
