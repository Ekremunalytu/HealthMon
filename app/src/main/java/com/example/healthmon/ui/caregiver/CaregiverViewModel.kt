package com.example.healthmon.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.HeartRateData
import com.example.healthmon.data.model.InactivityData
import com.example.healthmon.data.model.PatientInfo
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.CaregiverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Caregiver screen
 */
@HiltViewModel
class CaregiverViewModel @Inject constructor(
    private val repository: CaregiverRepository
) : ViewModel() {
    
    // List of patients assigned to this caregiver
    private val _patients = MutableStateFlow<List<PatientInfo>>(emptyList())
    val patients: StateFlow<List<PatientInfo>> = _patients.asStateFlow()

    // Currently selected patient ID
    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()
    
    // Real-time vital data
    private val _vitalDataState = MutableStateFlow(VitalDataState(isConnected = false))
    val vitalDataState: StateFlow<VitalDataState> = _vitalDataState.asStateFlow()
    
    private var vitalsJob: Job? = null
    
    /**
     * Load assigned patients from backend
     */
    fun loadPatients(caregiverId: String, token: String) {
        viewModelScope.launch {
            repository.getAssignedPatients(caregiverId, token)
                .onSuccess { patientList ->
                    _patients.value = patientList
                    // Auto-select first patient if none selected
                    if (_selectedPatientId.value == null && patientList.isNotEmpty()) {
                        selectPatient(patientList.first().id, token)
                    }
                }
                .onFailure {
                    // Handle error (expose to UI if needed)
                }
        }
    }
    
    /**
     * Select a patient and subscribe to their vitals
     */
    fun selectPatient(patientId: String, token: String) {
        if (_selectedPatientId.value == patientId) return
        
        _selectedPatientId.value = patientId
        
        // Cancel previous subscription
        vitalsJob?.cancel()
        
        // Start new subscription
        vitalsJob = viewModelScope.launch {
            // Reset state while connecting
            _vitalDataState.value = VitalDataState(isConnected = false)
            
            repository.connectToPatientVitals(patientId, token).collect { wsData ->
                _vitalDataState.value = VitalDataState(
                    heartRate = HeartRateData(bpm = wsData.heartRate),
                    inactivity = InactivityData(durationMinutes = wsData.inactivitySeconds / 60),
                    isConnected = true
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.disconnectWebSocket()
    }
}
