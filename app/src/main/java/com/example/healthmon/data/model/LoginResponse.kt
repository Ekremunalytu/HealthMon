package com.example.healthmon.data.model

import com.squareup.moshi.Json

data class LoginResponse(
    @Json(name = "message") val message: String,
    @Json(name = "access_token") val accessToken: String? // Token olabilir veya olmayabilir
)
