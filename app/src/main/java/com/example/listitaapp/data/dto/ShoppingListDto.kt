package com.example.listitaapp.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateShoppingListRequest(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("recurring")
    val recurring: Boolean = false,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class UpdateShoppingListRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("recurring")
    val recurring: Boolean? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class ShareByEmailRequest(
    @SerialName("email")
    val email: String
)

@Serializable
data class AddListItemRequest(
    @SerialName("product")
    val product: ItemProduct,
    @SerialName("quantity")
    val quantity: Double,
    @SerialName("unit")
    val unit: String,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class ItemProduct(
    @SerialName("id")
    val id: Long
)

@Serializable
data class UpdateListItemRequest(
    @SerialName("quantity")
    val quantity: Double? = null,
    @SerialName("unit")
    val unit: String? = null,
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class AddListItemResponse(
    @SerialName("item")
    val item: com.example.listitaapp.data.model.ListItem
)

@Serializable
data class ToggleItemPurchasedRequest(
    @SerialName("purchased")
    val purchased: Boolean
)

@Serializable
data class PurchaseListRequest(
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

@Serializable
data class RestorePurchaseResponse(
    @SerialName("list")
    val list: com.example.listitaapp.data.model.ShoppingList
)
