package com.example.listitaapp.data.dto

import com.example.listitaapp.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("token")
    val token: String
)

@Serializable
data class RegisterRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("name")
    val name: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

typealias RegisterResponse = User

@Serializable
data class VerifyAccountRequest(
    @SerialName("code")
    val code: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("currentPassword")
    val currentPassword: String,
    @SerialName("newPassword")
    val newPassword: String
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("name")
    val name: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class PasswordRecoveryRequest(
    @SerialName("email")
    val email: String
)

@Serializable
data class PasswordResetRequest(
    @SerialName("code")
    val code: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class ErrorResponse(
    @SerialName("message")
    val message: String
)

@Serializable
data class ResendVerificationResponse(
    @SerialName("code")
    val code: String
)
