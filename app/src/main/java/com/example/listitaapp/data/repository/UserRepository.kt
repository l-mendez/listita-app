package com.example.listitaapp.data.repository

import com.example.listitaapp.data.datasource.UserRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.domain.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource
) {

    suspend fun getProfile(): Result<User> = runCatching {
        remoteDataSource.getProfile().toDomain()
    }

    suspend fun updateProfile(name: String, surname: String): Result<User> = runCatching {
        remoteDataSource.updateProfile(name, surname).toDomain()
    }
}
