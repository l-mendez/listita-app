package com.example.listitaapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShoppingList(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "recurring")
    val recurring: Boolean = false,

    @Json(name = "owner")
    val owner: User,

    @Json(name = "sharedWith")
    val sharedWith: List<User> = emptyList(),

    @Json(name = "lastPurchasedAt")
    val lastPurchasedAt: String? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "updatedAt")
    val updatedAt: String
)
