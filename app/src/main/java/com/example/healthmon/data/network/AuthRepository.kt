package com.example.healthmon.data.network

import com.example.healthmon.data.model.LoginRequest
import com.example.healthmon.data.model.LoginResponse
import com.example.healthmon.data.service.ApiService
import kotlinx.coroutines.delay
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    // Set to true to use mock authentication (no backend required)
    private val useMockAuth = true
    
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        // Use mock authentication for development/testing
        if (useMockAuth) {
            return mockLogin(request)
        }
        
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Login failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun mockLogin(request: LoginRequest): Result<LoginResponse> {
        // Simulate network delay
        delay(500)
        
        // Mock credentials:
        // patient/patient -> Hasta login
        // caregiver/caregiver -> Hasta Bakıcı login
        return when {
            request.username == "patient" && request.password == "patient" -> {
                Result.success(LoginResponse(
                    success = true,
                    message = "Login successful",
                    token = "mock_token_patient_12345",
                    userType = "Hasta"
                ))
            }
            request.username == "caregiver" && request.password == "caregiver" -> {
                Result.success(LoginResponse(
                    success = true,
                    message = "Login successful",
                    token = "mock_token_caregiver_12345",
                    userType = "Hasta Bakıcı"
                ))
            }
            else -> {
                Result.failure(Exception(
                    "Hatalı kullanıcı adı veya şifre!\n(Test için: patient/patient veya caregiver/caregiver)"
                ))
            }
        }
    }
}
