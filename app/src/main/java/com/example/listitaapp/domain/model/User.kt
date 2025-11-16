package com.example.listitaapp.domain.model

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val surname: String,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val updatedAt: String
)
