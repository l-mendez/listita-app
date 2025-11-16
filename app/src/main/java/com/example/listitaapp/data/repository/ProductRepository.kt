package com.example.listitaapp.data.repository

import com.example.listitaapp.data.datasource.CategoryRemoteDataSource
import com.example.listitaapp.data.datasource.ProductRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.domain.model.Category
import com.example.listitaapp.domain.model.Product
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productRemoteDataSource: ProductRemoteDataSource,
    private val categoryRemoteDataSource: CategoryRemoteDataSource
) {

    suspend fun getProducts(
        name: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): Result<List<Product>> = runCatching {
        productRemoteDataSource
            .getProducts(name, categoryId, page, perPage, sortBy, order)
            .data
            .map { it.toDomain() }
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

    suspend fun updateCategory(id: Long, name: String): Result<Category> = runCatching {
        categoryRemoteDataSource.updateCategory(id, name).toDomain()
    }

    suspend fun deleteCategory(id: Long): Result<Unit> = runCatching {
        categoryRemoteDataSource.deleteCategory(id)
    }
}
