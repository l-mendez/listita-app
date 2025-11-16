package com.example.listitaapp.domain.model

data class ShoppingList(
    val id: Long,
    val name: String,
    val description: String? = null,
    val recurring: Boolean = false,
    val owner: User,
    val sharedWith: List<User> = emptyList(),
    val lastPurchasedAt: String? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val updatedAt: String
)
