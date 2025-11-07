package com.example.listitaapp.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Create Product Request
@JsonClass(generateAdapter = true)
data class CreateProductRequest(
    @Json(name = "name")
    val name: String,

    @Json(name = "category")
    val category: ProductCategory? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ProductCategory(
    @Json(name = "id")
    val id: Long
)

// Update Product Request
@JsonClass(generateAdapter = true)
data class UpdateProductRequest(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "category")
    val category: ProductCategory? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Create Category Request
@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    @Json(name = "name")
    val name: String,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Update Category Request
@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)
