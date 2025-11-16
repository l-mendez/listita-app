package com.example.listitaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null,
    @SerialName("owner")
    val owner: User? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String
)
