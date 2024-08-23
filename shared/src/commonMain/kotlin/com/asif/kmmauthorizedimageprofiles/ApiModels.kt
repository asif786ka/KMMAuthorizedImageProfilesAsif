package com.asif.kmmauthorizedimageprofiles

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AvatarRequest(val avatar: String)

@Serializable
data class TokenResponse(val userid: String, val token: String)

@Serializable
data class ProfileResponse(val email: String, val avatar_url: String?)

@Serializable
data class AvatarResponse(val avatar_url: String)

@Serializable
data class ErrorResponse(val error: String? = null)
