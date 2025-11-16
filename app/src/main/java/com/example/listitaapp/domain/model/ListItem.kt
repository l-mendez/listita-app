package com.example.listitaapp.domain.model

data class ListItem(
    val id: Long,
    val quantity: Double,
    val unit: String,
    val purchased: Boolean = false,
    val lastPurchasedAt: String? = null,
    val product: Product? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val updatedAt: String
)
