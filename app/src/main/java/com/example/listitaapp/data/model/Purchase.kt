package com.example.listitaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    @SerialName("id")
    val id: Long,
    @SerialName("list")
    val list: ShoppingList? = null,
    @SerialName("owner")
    val owner: User? = null,
    @SerialName("items")
    val items: List<ListItem> = emptyList(),
    @SerialName("metadata")
    val metadata: Map<String, String>? = null,
    @SerialName("createdAt")
    val createdAt: String
)
