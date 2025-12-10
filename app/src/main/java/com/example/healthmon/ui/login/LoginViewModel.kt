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
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String, userType: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val request = LoginRequest(username = username, password = password, userType = userType)
            val result = repository.login(request)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = LoginUiState.Success(userType)
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val userType: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
