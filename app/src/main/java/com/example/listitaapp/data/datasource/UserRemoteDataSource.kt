package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.UpdateProfileRequest
import com.example.listitaapp.data.model.User
import com.squareup.moshi.Moshi
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    moshi: Moshi
) : BaseRemoteDataSource(moshi) {

    suspend fun getProfile(): User {
        val response = apiService.getProfile()
        return handleResponse(response, "Failed to get profile")
    }

    suspend fun updateProfile(name: String, surname: String): User {
        val response = apiService.updateProfile(UpdateProfileRequest(name, surname))
        return handleResponse(response, "Failed to update profile")
    }
}
