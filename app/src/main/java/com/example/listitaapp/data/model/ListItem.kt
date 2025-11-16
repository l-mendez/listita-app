package com.example.listitaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListItem(
    @SerialName("id")
    val id: Long,
    @SerialName("quantity")
    val quantity: Double,
    @SerialName("unit")
    val unit: String,
    @SerialName("purchased")
    val purchased: Boolean = false,
    @SerialName("lastPurchasedAt")
    val lastPurchasedAt: String? = null,
    @SerialName("product")
    val product: Product? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String
)
