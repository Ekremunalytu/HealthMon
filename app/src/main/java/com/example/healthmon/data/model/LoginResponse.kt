package com.example.healthmon.data.model

import com.squareup.moshi.Json

data class LoginResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String,
    @Json(name = "access_token") val token: String? = null,
    @Json(name = "user_type") val userType: String? = null
)
