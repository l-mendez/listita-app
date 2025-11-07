package com.example.listitaapp.data.dto

import com.example.listitaapp.data.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Login Request
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String
)

// Login Response
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token")
    val token: String
)

// Register Request
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "surname")
    val surname: String,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Register Response - API returns User object directly
// Verification token is sent via email, not in response
typealias RegisterResponse = User

// Verify Account Request
@JsonClass(generateAdapter = true)
data class VerifyAccountRequest(
    @Json(name = "code")
    val code: String
)

// Change Password Request
@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "currentPassword")
    val currentPassword: String,

    @Json(name = "newPassword")
    val newPassword: String
)

// Update Profile Request
@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "name")
    val name: String,

    @Json(name = "surname")
    val surname: String,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Password Recovery Request
@JsonClass(generateAdapter = true)
data class PasswordRecoveryRequest(
    @Json(name = "email")
    val email: String
)

// Password Reset Request
@JsonClass(generateAdapter = true)
data class PasswordResetRequest(
    @Json(name = "code")
    val code: String,

    @Json(name = "password")
    val password: String
)

// Error Response from API
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "message")
    val message: String
)

// Resend Verification Response
@JsonClass(generateAdapter = true)
data class ResendVerificationResponse(
    @Json(name = "code")
    val code: String
)
