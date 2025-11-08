package com.example.listitaapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListItem(
    @Json(name = "id")
    val id: Long,

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "unit")
    val unit: String,

    @Json(name = "purchased")
    val purchased: Boolean = false,

    @Json(name = "lastPurchasedAt")
    val lastPurchasedAt: String? = null,

    @Json(name = "product")
    val product: Product? = null,  // Nullable because product might be deleted

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "updatedAt")
    val updatedAt: String
)
