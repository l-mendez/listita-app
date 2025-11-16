package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.CreateProductRequest
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.dto.ProductCategory
import com.example.listitaapp.data.dto.UpdateProductRequest
import com.example.listitaapp.data.dto.UpdateProductResponse
import com.example.listitaapp.data.model.Product
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ProductRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun getProducts(
        name: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): PaginatedResponse<Product> {
        val response = apiService.getProducts(name, categoryId, page, perPage, sortBy, order)
        return handleResponse(response, "Failed to get products")
    }

    suspend fun createProduct(name: String, categoryId: Long?): Product {
        val request = CreateProductRequest(name, categoryId?.let { ProductCategory(it) })
        val response = apiService.createProduct(request)
        return handleResponse(response, "Failed to create product")
    }

    suspend fun getProductById(id: Long): Product {
        val response = apiService.getProductById(id)
        return handleResponse(response, "Failed to get product")
    }

    suspend fun updateProduct(id: Long, name: String?, categoryId: Long?): UpdateProductResponse {
        val request = UpdateProductRequest(
            name = name,
            category = categoryId?.let { ProductCategory(it) }
        )
        val response = apiService.updateProduct(id, request)
        return handleResponse(response, "Failed to update product")
    }

    suspend fun deleteProduct(id: Long) {
        val response = apiService.deleteProduct(id)
        handleUnitResponse(response, "Failed to delete product")
    }
}
