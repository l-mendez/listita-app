package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.PaginatedResponse
import com.example.listitaapp.data.dto.PurchaseListRequest
import com.example.listitaapp.data.dto.ShareByEmailRequest
import com.example.listitaapp.data.dto.UpdateShoppingListRequest
import com.example.listitaapp.data.dto.CreateShoppingListRequest
import com.example.listitaapp.data.model.ShoppingList
import com.example.listitaapp.data.model.User
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ShoppingListRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    json: Json
) : BaseRemoteDataSource(json) {

    suspend fun getShoppingLists(
        name: String? = null,
        owner: Boolean? = null,
        recurring: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): PaginatedResponse<ShoppingList> {
        val response = apiService.getShoppingLists(name, owner, recurring, page, perPage, sortBy, order)
        return handleResponse(response, "Failed to get shopping lists")
    }

    suspend fun createShoppingList(name: String, description: String?, recurring: Boolean): ShoppingList {
        val request = CreateShoppingListRequest(name, description, recurring)
        val response = apiService.createShoppingList(request)
        return handleResponse(response, "Failed to create list")
    }

    suspend fun getShoppingListById(id: Long): ShoppingList {
        val response = apiService.getShoppingListById(id)
        return handleResponse(response, "Failed to get shopping list")
    }

    suspend fun updateShoppingListName(id: Long, name: String): ShoppingList {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(name = name))
        return handleResponse(response, "Failed to update list name")
    }

    suspend fun updateShoppingListDescription(id: Long, description: String): ShoppingList {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(description = description))
        return handleResponse(response, "Failed to update list description")
    }

    suspend fun toggleShoppingListRecurring(id: Long, recurring: Boolean): ShoppingList {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(recurring = recurring))
        return handleResponse(response, "Failed to toggle recurring status")
    }

    suspend fun deleteShoppingList(id: Long) {
        val response = apiService.deleteShoppingList(id)
        handleUnitResponse(response, "Failed to delete list")
    }

    suspend fun shareListWithEmail(listId: Long, email: String) {
        val response = apiService.shareShoppingListByEmail(listId, ShareByEmailRequest(email))
        handleUnitResponse(response, "Failed to share list")
    }

    suspend fun getSharedUsers(listId: Long): List<User> {
        val response = apiService.getSharedUsers(listId)
        return handleResponse(response, "Failed to get shared users")
    }

    suspend fun revokeShare(listId: Long, userId: Long) {
        val response = apiService.revokeShare(listId, userId)
        handleUnitResponse(response, "Failed to revoke share")
    }

    suspend fun purchaseList(listId: Long) {
        val response = apiService.purchaseList(listId, PurchaseListRequest())
        handleUnitResponse(response, "Failed to complete purchase")
    }
}
