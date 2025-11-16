package com.example.listitaapp.domain.model

data class Product(
    val id: Long,
    val name: String,
    val category: Category? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val updatedAt: String
)
