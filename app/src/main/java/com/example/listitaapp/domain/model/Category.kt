package com.example.listitaapp.domain.model

data class Category(
    val id: Long,
    val name: String,
    val metadata: Map<String, String>? = null,
    val owner: User? = null,
    val createdAt: String,
    val updatedAt: String
)
