package com.example.listitaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("recurring")
    val recurring: Boolean = false,
    @SerialName("owner")
    val owner: User,
    @SerialName("sharedWith")
    val sharedWith: List<User> = emptyList(),
    @SerialName("lastPurchasedAt")
    val lastPurchasedAt: String? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String
)
