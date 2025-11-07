package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.api.TokenManager
import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    // Moshi instance for parsing error responses
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Extract error message from API response body
     */
    private fun <T> getErrorMessage(response: Response<T>, defaultMessage: String): String {
        return try {
            response.errorBody()?.string()?.let { errorBody ->
                val adapter = moshi.adapter(ErrorResponse::class.java)
                adapter.fromJson(errorBody)?.message
            } ?: response.message().ifEmpty { defaultMessage }
        } catch (e: Exception) {
            response.message().ifEmpty { defaultMessage }
        }
    }

    /**
     * Register a new user account
     * Returns the created User object. Verification code is sent via email.
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        surname: String
    ): Result<User> {
        return try {
            val request = RegisterRequest(email, password, name, surname)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = getErrorMessage(response, "Registration failed")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                tokenManager.saveToken(token)
                Result.success(token)
            } else {
                val errorMessage = getErrorMessage(response, "Login failed")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify account with code
     * Returns the verified User object. User must login separately to get a token.
     */
    suspend fun verifyAccount(code: String): Result<User> {
        return try {
            val trimmedCode = code.trim()
            android.util.Log.d("AuthRepository", "Verifying with code: '$trimmedCode' (length: ${trimmedCode.length})")
            val request = VerifyAccountRequest(trimmedCode)
            val response = apiService.verifyAccount(request)

            android.util.Log.d("AuthRepository", "Response code: ${response.code()}, successful: ${response.isSuccessful}, body null: ${response.body() == null}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("AuthRepository", "Verification successful! User: ${response.body()}")
                Result.success(response.body()!!)
            } else if (response.isSuccessful && response.body() == null) {
                // Response was 200 but body is null - possible parsing error
                android.util.Log.e("AuthRepository", "Verification response was successful but body is null - JSON parsing may have failed")
                Result.failure(Exception("Verification succeeded but response parsing failed"))
            } else {
                val errorMessage = getErrorMessage(response, "Verification failed")
                android.util.Log.e("AuthRepository", "Verification failed: $errorMessage (code: ${response.code()})")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Verification exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Resend verification code
     * Returns the new verification code
     */
    suspend fun resendVerification(email: String): Result<String> {
        return try {
            val response = apiService.resendVerification(email)

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("AuthRepository", "Verification code resent: ${response.body()?.code}")
                Result.success(response.body()!!.code)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to resend verification")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change password
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = apiService.changePassword(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to change password")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Request password recovery
     */
    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val request = PasswordRecoveryRequest(email)
            val response = apiService.forgotPassword(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to request password recovery")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reset password with code
     */
    suspend fun resetPassword(code: String, newPassword: String): Result<Unit> {
        return try {
            val request = PasswordResetRequest(code, newPassword)
            val response = apiService.resetPassword(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to reset password")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user profile
     */
    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to get profile")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(name: String, surname: String): Result<User> {
        return try {
            val request = UpdateProfileRequest(name, surname)
            val response = apiService.updateProfile(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = getErrorMessage(response, "Failed to update profile")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout (clear local auth data)
     */
    suspend fun logout() {
        tokenManager.clearAuth()
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }
}
