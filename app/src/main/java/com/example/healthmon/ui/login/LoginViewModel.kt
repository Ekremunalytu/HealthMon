package com.example.healthmon.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmon.data.model.LoginRequest
import com.example.healthmon.data.network.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: com.example.healthmon.data.local.TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val request = LoginRequest(username = username, password = password, role = role)
            val result = repository.login(request)

            result.fold(
                onSuccess = { response ->
                    // Determine user type and validate IDs to prevent navigation crashes
                    val rawUserType = response.userType
                    val patientId = response.patientId
                    val caregiverId = response.caregiverId
                    val token = response.token

                    if (token.isNullOrEmpty()) {
                        _uiState.value = LoginUiState.Error("Giriş başarılı fakat token alınamadı (Backend Hatası).")
                        return@fold
                    }

                    // Logic to determine type if not explicit or if mismatch
                    val finalUserType = when {
                        !patientId.isNullOrEmpty() -> "Hasta" // If patientId exists, treat as patient
                        !caregiverId.isNullOrEmpty() -> "Hasta Bakıcı" // If caregiverId exists, treat as caregiver
                        rawUserType == "patient" -> "Hasta"
                        rawUserType == "caregiver" -> "Hasta Bakıcı"
                        else -> rawUserType ?: "Hasta" // Default to Hasta if unknown
                    }

                    // Validate required IDs based on type
                    if (finalUserType == "Hasta" && patientId.isNullOrEmpty()) {
                        _uiState.value = LoginUiState.Error("Giriş başarılı fakat Hasta ID bulunamadı.")
                        return@fold
                    }

                    if (finalUserType == "Hasta Bakıcı" && caregiverId.isNullOrEmpty()) {
                        _uiState.value = LoginUiState.Error("Giriş başarılı fakat Bakıcı ID bulunamadı.")
                        return@fold
                    }

                    // Save persistence data
                    tokenManager.saveAuthData(
                        token = token,
                        userType = finalUserType,
                        userId = response.userId,
                        patientId = patientId,
                        caregiverId = caregiverId
                    )

                    _uiState.value = LoginUiState.Success(
                        userType = finalUserType, // This will be "Hasta" or "Hasta Bakıcı"
                        token = token,
                        patientId = patientId,
                        caregiverId = caregiverId
                    )
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(
        val userType: String,
        val token: String? = null,
        val patientId: String? = null,
        val caregiverId: String? = null
    ) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
