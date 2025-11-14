package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.Category
import com.example.listitaapp.data.model.Product
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getProducts(): Result<List<Product>> = try {
        val response = apiService.getProducts()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createProduct(name: String, categoryId: Long?): Result<Product> = try {
        val request = CreateProductRequest(name, categoryId?.let { ProductCategory(it) })
        val response = apiService.createProduct(request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProduct(
        id: Long,
        name: String?,
        categoryId: Long?,
        metadata: Map<String, Any>?
    ): Result<Product> = try {
        val request = UpdateProductRequest(
            name = name,
            category = categoryId?.let { ProductCategory(it) },
            metadata = metadata
        )
        val response = apiService.updateProduct(id, request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProduct(id: Long): Result<Unit> = try {
        val response = apiService.deleteProduct(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCategories(): Result<List<Category>> = try {
        val response = apiService.getCategories()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createCategory(name: String): Result<Category> = try {
        val request = CreateCategoryRequest(name)
        val response = apiService.createCategory(request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCategory(id: Long, name: String): Result<Category> = try {
        val request = UpdateCategoryRequest(name = name)
        val response = apiService.updateCategory(id, request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteCategory(id: Long): Result<Unit> = try {
        val response = apiService.deleteCategory(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
