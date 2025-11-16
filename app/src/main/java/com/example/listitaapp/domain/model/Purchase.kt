package com.example.listitaapp.domain.model

data class Purchase(
    val id: Long,
    val list: ShoppingList? = null,
    val owner: User? = null,
    val items: List<ListItem> = emptyList(),
    val metadata: Map<String, Any>? = null,
    val createdAt: String
)
