package com.example.listitaapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Purchase(
    @Json(name = "id")
    val id: Long,

    @Json(name = "list")
    val list: ShoppingList? = null,

    @Json(name = "owner")
    val owner: User? = null,

    @Json(name = "items")
    val items: List<ListItem> = emptyList(),

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null,

    @Json(name = "createdAt")
    val createdAt: String
)
