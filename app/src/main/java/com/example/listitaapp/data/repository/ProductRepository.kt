package com.example.listitaapp.data.repository

import com.example.listitaapp.data.datasource.CategoryRemoteDataSource
import com.example.listitaapp.data.datasource.ProductRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.data.mapper.toDomain as mapPaginated
import com.example.listitaapp.domain.model.Category
import com.example.listitaapp.domain.model.Product
import com.example.listitaapp.domain.model.PaginatedResult
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productRemoteDataSource: ProductRemoteDataSource,
    private val categoryRemoteDataSource: CategoryRemoteDataSource
) {

    suspend fun getProducts(
        name: String? = null,
        page: Int = 1,
        perPage: Int = 10
    ): Result<PaginatedResult<Product>> = runCatching {
        val response = productRemoteDataSource
            .getProducts(name, categoryId = null, page, perPage, sortBy = "createdAt", order = "DESC")
        response.mapPaginated { it.toDomain() }
    }

    suspend fun createProduct(name: String, categoryId: Long?): Result<Product> = runCatching {
        productRemoteDataSource.createProduct(name, categoryId).toDomain()
    }

    suspend fun updateProduct(
        id: Long,
        name: String?,
        categoryId: Long?
    ): Result<Product> = runCatching {
        productRemoteDataSource.updateProduct(id, name, categoryId).product.toDomain()
    }

    suspend fun deleteProduct(id: Long): Result<Unit> = runCatching {
        productRemoteDataSource.deleteProduct(id)
    }

    suspend fun getCategories(): Result<List<Category>> = runCatching {
        categoryRemoteDataSource.getCategories().data.map { it.toDomain() }
    }

    suspend fun createCategory(name: String): Result<Category> = runCatching {
        categoryRemoteDataSource.createCategory(name).toDomain()
    }
}
