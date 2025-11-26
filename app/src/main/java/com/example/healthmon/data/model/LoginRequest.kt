package com.example.healthmon.data.model

import com.squareup.moshi.Json

data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "user_type") val userType: String
)
