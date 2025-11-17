package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.CreateCategoryRequest
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.model.Category
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CategoryRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun getCategories(): PaginatedResponse<Category> {
        val response = apiService.getCategories()
        return handleResponse(response, "Failed to get categories")
    }

    suspend fun createCategory(name: String): Category {
        val response = apiService.createCategory(CreateCategoryRequest(name))
        return handleResponse(response, "Failed to create category")
    }
}
