package com.example.healthmon.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.VitalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for Caregiver screen
 */
@HiltViewModel
class CaregiverViewModel @Inject constructor(
    private val vitalDataRepository: VitalDataRepository
) : ViewModel() {
    
    /**
     * Currently selected patient index
     */
    private val _selectedPatientIndex = MutableStateFlow(0)
    val selectedPatientIndex: StateFlow<Int> = _selectedPatientIndex.asStateFlow()
    
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
     * Select a patient by index
     */
    fun selectPatient(index: Int) {
        _selectedPatientIndex.value = index
    }
}
