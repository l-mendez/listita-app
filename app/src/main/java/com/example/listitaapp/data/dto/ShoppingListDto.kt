package com.example.listitaapp.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Create Shopping List Request
@JsonClass(generateAdapter = true)
data class CreateShoppingListRequest(
    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "recurring")
    val recurring: Boolean = false,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Update Shopping List Request
@JsonClass(generateAdapter = true)
data class UpdateShoppingListRequest(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "recurring")
    val recurring: Boolean? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Share List Request
@JsonClass(generateAdapter = true)
data class ShareListRequest(
    @Json(name = "users")
    val users: List<ShareUser>
)

@JsonClass(generateAdapter = true)
data class ShareByEmailRequest(
    @Json(name = "email")
    val email: String
)

@JsonClass(generateAdapter = true)
data class ShareUser(
    @Json(name = "id")
    val id: Long
)

// Add List Item Request
@JsonClass(generateAdapter = true)
data class AddListItemRequest(
    @Json(name = "product")
    val product: ItemProduct,

    @Json(name = "quantity")
    val quantity: Double,

    @Json(name = "unit")
    val unit: String,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ItemProduct(
    @Json(name = "id")
    val id: Long
)

// Update List Item Request
@JsonClass(generateAdapter = true)
data class UpdateListItemRequest(
    @Json(name = "quantity")
    val quantity: Double? = null,

    @Json(name = "unit")
    val unit: String? = null,

    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Add List Item Response (API returns wrapper)
@JsonClass(generateAdapter = true)
data class AddListItemResponse(
    @Json(name = "item")
    val item: com.example.listitaapp.data.model.ListItem
)

// Toggle Item Purchased Request
@JsonClass(generateAdapter = true)
data class ToggleItemPurchasedRequest(
    @Json(name = "purchased")
    val purchased: Boolean
)

// Purchase List Request
@JsonClass(generateAdapter = true)
data class PurchaseListRequest(
    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

// Restore Purchase Response
@JsonClass(generateAdapter = true)
data class RestorePurchaseResponse(
    @Json(name = "list")
    val list: com.example.listitaapp.data.model.ShoppingList
)
