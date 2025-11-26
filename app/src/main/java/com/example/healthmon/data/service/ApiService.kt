package com.example.healthmon.data.service

import com.example.healthmon.data.model.LoginRequest
import com.example.healthmon.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/login/") // FastAPI endpoint'inizin yolu
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
