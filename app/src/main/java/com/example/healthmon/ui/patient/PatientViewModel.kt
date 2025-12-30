package com.example.healthmon.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.VitalDataRepository
import com.example.healthmon.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Patient screen
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val vitalDataRepository: VitalDataRepository,
    private val patientRepository: PatientRepository,
    private val tokenManager: com.example.healthmon.data.local.TokenManager
) : ViewModel() {
    
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
     * Start sending data to backend
     */
    fun startSendingData(patientId: String, token: String) {
        // Connect to WebSocket first
        patientRepository.connectWebSocket(patientId, token) {
            // After connection, start sending loop
            viewModelScope.launch {
                vitalDataState.collect { data ->
                    if (data.isConnected) { // Only send if we have valid data
                        patientRepository.sendMeasurementWebSocket(
                            patientId = patientId,
                            heartRate = data.heartRate.bpm,
                            inactivitySeconds = data.inactivity.durationMinutes * 60,
                            status = if (data.heartRate.bpm !in 60..100) "WARNING" else "NORMAL"
                        )
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
}
