package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.AddListItemRequest
import com.example.listitaapp.data.dto.ItemProduct
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.dto.ToggleItemPurchasedRequest
import com.example.listitaapp.data.dto.UpdateListItemRequest
import com.example.listitaapp.data.model.ListItem
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ListItemRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun getListItems(
        listId: Long,
        purchased: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "createdAt",
        order: String = "DESC",
        pantryId: Long? = null,
        categoryId: Long? = null,
        search: String? = null
    ): PaginatedResponse<ListItem> {
        val response = apiService.getListItems(
            listId = listId,
            purchased = purchased,
            page = page,
            perPage = perPage,
            sortBy = sortBy,
            order = order,
            pantryId = pantryId,
            categoryId = categoryId,
            search = search
        )
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
        metadata: Map<String, String>? = null
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
        return getListItems(listId, page = 1, perPage = 1).pagination.total
    }
}
