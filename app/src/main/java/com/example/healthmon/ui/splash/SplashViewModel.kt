package com.example.healthmon.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = tokenManager.authToken.first()
            val userType = tokenManager.userType.first()
            val patientId = tokenManager.patientId.first()
            val caregiverId = tokenManager.caregiverId.first()

            if (!token.isNullOrEmpty() && !userType.isNullOrEmpty()) {
                // Determine destination
                if (userType == "Hasta" && !patientId.isNullOrEmpty()) {
                    _uiState.value = SplashUiState.NavigateToPatient(token, patientId)
                } else if (userType == "Hasta Bakıcı" && !caregiverId.isNullOrEmpty()) {
                    _uiState.value = SplashUiState.NavigateToCaregiver(token, caregiverId)
                } else {
                    _uiState.value = SplashUiState.NavigateToLogin
                }
            } else {
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }
}

sealed class SplashUiState {
    object Loading : SplashUiState()
    object NavigateToLogin : SplashUiState()
    data class NavigateToPatient(val token: String, val patientId: String) : SplashUiState()
    data class NavigateToCaregiver(val token: String, val caregiverId: String) : SplashUiState()
}
