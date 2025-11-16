package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.AuthResponse
import com.example.listitaapp.data.dto.ChangePasswordRequest
import com.example.listitaapp.data.dto.LoginRequest
import com.example.listitaapp.data.dto.PasswordRecoveryRequest
import com.example.listitaapp.data.dto.PasswordResetRequest
import com.example.listitaapp.data.dto.RegisterRequest
import com.example.listitaapp.data.dto.ResendVerificationResponse
import com.example.listitaapp.data.dto.VerifyAccountRequest
import com.example.listitaapp.data.model.User
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun register(
        email: String,
        password: String,
        name: String,
        surname: String
    ): User {
        val request = RegisterRequest(email, password, name, surname)
        val response = apiService.register(request)
        return handleResponse(response, "Registration failed")
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val response = apiService.login(LoginRequest(email, password))
        return handleResponse(response, "Login failed")
    }

    suspend fun verifyAccount(code: String): User {
        val response = apiService.verifyAccount(VerifyAccountRequest(code))
        return handleResponse(response, "Verification failed")
    }

    suspend fun resendVerification(email: String): ResendVerificationResponse {
        val response = apiService.resendVerification(email)
        return handleResponse(response, "Failed to resend verification")
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        handleUnitResponse(response, "Failed to change password")
    }

    suspend fun forgotPassword(email: String) {
        val response = apiService.forgotPassword(PasswordRecoveryRequest(email))
        handleUnitResponse(response, "Failed to request password recovery")
    }

    suspend fun resetPassword(code: String, newPassword: String) {
        val response = apiService.resetPassword(PasswordResetRequest(code, newPassword))
        handleUnitResponse(response, "Failed to reset password")
    }

    suspend fun logout() {
        val response = apiService.logout()
        handleUnitResponse(response, "Failed to logout")
    }
}
