package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getProfile(): Result<User> = try {
        val response = apiService.getProfile()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(name: String, surname: String): Result<User> = try {
        val response = apiService.updateProfile(
            com.example.listitaapp.data.dto.UpdateProfileRequest(name, surname)
        )
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = try {
        val response = apiService.changePassword(
            com.example.listitaapp.data.dto.ChangePasswordRequest(currentPassword, newPassword)
        )
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
