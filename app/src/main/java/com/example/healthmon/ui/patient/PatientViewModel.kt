package com.example.healthmon.ui.patient

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.VitalDataRepository
import com.example.healthmon.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Patient screen
 * Connects to backend via WebSocket and receives real-time vital data
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val vitalDataRepository: VitalDataRepository,
    private val patientRepository: PatientRepository,
    private val tokenManager: com.example.healthmon.data.local.TokenManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "PatientViewModel"
    }
    
    // UI State
    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()
    
    // Data transmission state (for UI indicator)
    private val _dataTransmissionState = MutableStateFlow(DataTransmissionState())
    val dataTransmissionState: StateFlow<DataTransmissionState> = _dataTransmissionState.asStateFlow()
    
    /**
     * Vital data state flow for UI observation
     */
    val vitalDataState: StateFlow<VitalDataState> = vitalDataRepository.getVitalDataStateFlow()
    
    /**
     * Start data streaming - connects to backend and starts receiving/sending data
     * This is the main entry point called from PatientScreen
     */
    fun startDataStreaming(patientId: String, token: String) {
        Log.d(TAG, "Starting data streaming for patient: $patientId")
        
        _dataTransmissionState.value = _dataTransmissionState.value.copy(
            isConnecting = true,
            connectionStatus = "Bağlanıyor..."
        )
        
        // Connect to backend to receive data
        connectToBackend(patientId, token)
        
        // Also start sending data to backend
        startSendingData(patientId, token)
    }
    
    /**
     * Connect to backend and start receiving live vital data
     */
    private fun connectToBackend(patientId: String, token: String) {
        Log.d(TAG, "Connecting to backend for patient: $patientId")
        
        _uiState.value = _uiState.value.copy(isConnecting = true)
        
        viewModelScope.launch {
            try {
                vitalDataRepository.connectToBackend(patientId, token)
                    .catch { e ->
                        Log.e(TAG, "WebSocket error, falling back to mock data", e)
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            errorMessage = "Backend bağlantısı kurulamadı, simülasyon modu aktif"
                        )
                        _dataTransmissionState.value = _dataTransmissionState.value.copy(
                            isConnecting = false,
                            isConnected = false,
                            connectionStatus = "Simülasyon Modu"
                        )
                        // Fallback to mock data
                        startMockMode()
                    }
                    .collect { state ->
                        Log.d(TAG, "Received vital data: BPM=${state.heartRate.bpm}")
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            isConnected = true,
                            errorMessage = null
                        )
                        _dataTransmissionState.value = _dataTransmissionState.value.copy(
                            isConnecting = false,
                            isConnected = true,
                            connectionStatus = "Bağlı",
                            lastDataTime = System.currentTimeMillis()
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = "Bağlantı hatası: ${e.message}"
                )
                _dataTransmissionState.value = _dataTransmissionState.value.copy(
                    isConnecting = false,
                    isConnected = false,
                    connectionStatus = "Bağlantı Hatası"
                )
                startMockMode()
            }
        }
    }
    
    /**
     * Start mock data mode (for testing without backend)
     */
    private fun startMockMode() {
        viewModelScope.launch {
            Log.d(TAG, "Starting mock data mode")
            vitalDataRepository.startMockData()
        }
    }
    
    /**
     * Start sending data to backend via WebSocket
     */
    private fun startSendingData(patientId: String, token: String) {
        // Connect to WebSocket first
        patientRepository.connectWebSocket(patientId, token) {
            // After connection, start sending loop
            viewModelScope.launch {
                vitalDataState.collect { data ->
                    if (data.isConnected) {
                        val success = patientRepository.sendMeasurementWebSocket(
                            patientId = patientId,
                            heartRate = data.heartRate.bpm,
                            inactivitySeconds = data.inactivity.durationMinutes * 60,
                            status = if (data.heartRate.bpm !in 60..100) "WARNING" else "NORMAL"
                        )
                        if (success) {
                            _dataTransmissionState.value = _dataTransmissionState.value.copy(
                                lastDataTime = System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Logout user
     */
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearAuthData()
            patientRepository.disconnectWebSocket()
            onLogout()
        }
    }

    /**
     * Reset inactivity counter
     */
    fun resetInactivity() {
        vitalDataRepository.resetInactivity()
    }
    
    /**
     * Send emergency alert to backend
     */
    fun sendEmergency(patientId: String, message: String, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = patientRepository.triggerEmergency(patientId, message, token)
                result.onSuccess {
                    Log.d(TAG, "Emergency sent successfully")
                    onResult(true)
                }.onFailure { e ->
                    Log.e(TAG, "Failed to send emergency", e)
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception sending emergency", e)
                onResult(false)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * UI State for Patient screen
 */
data class PatientUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Data transmission state for connection indicator
 */
data class DataTransmissionState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val connectionStatus: String = "Bağlantı bekleniyor",
    val lastDataTime: Long = 0L
)
