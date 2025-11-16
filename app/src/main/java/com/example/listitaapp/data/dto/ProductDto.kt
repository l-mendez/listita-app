package com.example.listitaapp.data.dto

import com.example.listitaapp.data.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    @SerialName("name")
    val name: String,
    @SerialName("category")
    val category: ProductCategory? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class ProductCategory(
    @SerialName("id")
    val id: Long
)

@Serializable
data class UpdateProductRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("category")
    val category: ProductCategory? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class UpdateProductResponse(
    @SerialName("product")
    val product: Product
)

@Serializable
data class CreateCategoryRequest(
    @SerialName("name")
    val name: String,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class UpdateCategoryRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)
