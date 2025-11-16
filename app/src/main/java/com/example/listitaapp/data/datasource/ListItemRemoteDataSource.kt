package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.AddListItemRequest
import com.example.listitaapp.data.dto.ItemProduct
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.dto.ToggleItemPurchasedRequest
import com.example.listitaapp.data.dto.UpdateListItemRequest
import com.example.listitaapp.data.model.ListItem
import com.squareup.moshi.Moshi
import javax.inject.Inject

class ListItemRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    moshi: Moshi
) : BaseRemoteDataSource(moshi) {

    suspend fun getListItems(listId: Long): PaginatedResponse<ListItem> {
        val response = apiService.getListItems(listId)
        return handleResponse(response, "Failed to get list items")
    }

    suspend fun addListItem(
        listId: Long,
        productId: Long,
        quantity: Double,
        unit: String
    ): ListItem {
        val request = AddListItemRequest(ItemProduct(productId), quantity, unit)
        val response = apiService.addListItem(listId, request)
        val body = handleResponse(response, "Failed to add item")
        return body.item
    }

    suspend fun updateListItem(
        listId: Long,
        itemId: Long,
        quantity: Double,
        unit: String,
        metadata: Map<String, Any>? = null
    ): ListItem {
        val request = UpdateListItemRequest(
            quantity = quantity,
            unit = unit,
            metadata = metadata
        )
        val response = apiService.updateListItem(listId, itemId, request)
        return handleResponse(response, "Failed to update item")
    }

    suspend fun toggleItemPurchased(
        listId: Long,
        itemId: Long,
        purchased: Boolean
    ): ListItem {
        val request = ToggleItemPurchasedRequest(purchased)
        val response = apiService.toggleItemPurchased(listId, itemId, request)
        return handleResponse(response, "Failed to toggle item status")
    }

    suspend fun deleteListItem(listId: Long, itemId: Long) {
        val response = apiService.deleteListItem(listId, itemId)
        handleUnitResponse(response, "Failed to delete item")
    }

    suspend fun getListItemsCount(listId: Long): Int {
        return getListItems(listId).pagination.total
    }
}
