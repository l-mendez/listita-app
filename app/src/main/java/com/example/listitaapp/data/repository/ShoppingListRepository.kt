package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.ShoppingList

class ShoppingListRepository(private val apiService: ApiService) {

    suspend fun getShoppingLists(): Result<List<ShoppingList>> = try {
        val response = apiService.getShoppingLists()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createShoppingList(name: String, description: String?, recurring: Boolean): Result<ShoppingList> = try {
        val request = CreateShoppingListRequest(name, description, recurring)
        val response = apiService.createShoppingList(request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getShoppingListById(id: Long): Result<ShoppingList> = try {
        val response = apiService.getShoppingListById(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteShoppingList(id: Long): Result<Unit> = try {
        val response = apiService.deleteShoppingList(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getListItems(listId: Long): Result<List<ListItem>> = try {
        val response = apiService.getListItems(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addListItem(listId: Long, productId: Long, quantity: Double, unit: String): Result<ListItem> = try {
        val request = AddListItemRequest(ItemProduct(productId), quantity, unit)
        val response = apiService.addListItem(listId, request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleItemPurchased(listId: Long, itemId: Long): Result<ListItem> = try {
        val response = apiService.toggleItemPurchased(listId, itemId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteListItem(listId: Long, itemId: Long): Result<Unit> = try {
        val response = apiService.deleteListItem(listId, itemId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
