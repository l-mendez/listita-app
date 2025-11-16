package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.TokenManager
import com.example.listitaapp.data.datasource.AuthRemoteDataSource
import com.example.listitaapp.data.datasource.UserRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val tokenManager: TokenManager
) {

    suspend fun register(
        email: String,
        password: String,
        name: String,
        surname: String
    ): Result<User> = runCatching {
        authRemoteDataSource.register(email, password, name, surname).toDomain()
    }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val token = authRemoteDataSource.login(email, password).token
        tokenManager.saveToken(token)
        token
    }

    suspend fun verifyAccount(code: String): Result<User> = runCatching {
        authRemoteDataSource.verifyAccount(code.trim()).toDomain()
    }

    suspend fun resendVerification(email: String): Result<String> = runCatching {
        authRemoteDataSource.resendVerification(email).code
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        authRemoteDataSource.changePassword(currentPassword, newPassword)
    }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        authRemoteDataSource.forgotPassword(email)
    }

    suspend fun resetPassword(code: String, newPassword: String): Result<Unit> = runCatching {
        authRemoteDataSource.resetPassword(code, newPassword)
    }

    suspend fun getProfile(): Result<User> = runCatching {
        userRemoteDataSource.getProfile().toDomain()
    }

    suspend fun updateProfile(name: String, surname: String): Result<User> = runCatching {
        userRemoteDataSource.updateProfile(name, surname).toDomain()
    }

    suspend fun logout() {
        runCatching { authRemoteDataSource.logout() }
        tokenManager.clearAuth()
    }

    suspend fun isAuthenticated(): Boolean = tokenManager.isAuthenticated()

    fun authState(): Flow<Boolean> = tokenManager.getToken().map { it != null }
}
