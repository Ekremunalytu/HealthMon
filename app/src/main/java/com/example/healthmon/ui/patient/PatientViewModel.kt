package com.example.healthmon.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.VitalDataState
import com.example.healthmon.data.repository.VitalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for Patient screen
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val vitalDataRepository: VitalDataRepository
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
     * Reset inactivity counter
     */
    fun resetInactivity() {
        vitalDataRepository.resetInactivity()
    }
}
