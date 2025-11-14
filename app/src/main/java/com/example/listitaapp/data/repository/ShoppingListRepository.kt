package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.User
import com.example.listitaapp.data.model.ShoppingList
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getShoppingLists(): Result<List<ShoppingList>> = try {
        val response = apiService.getShoppingLists()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
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

    suspend fun updateShoppingListName(id: Long, name: String): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(name = name))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateShoppingListDescription(id: Long, description: String): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(description = description))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleShoppingListRecurring(id: Long, currentRecurring: Boolean): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(recurring = !currentRecurring))
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

    suspend fun shareListWithEmail(listId: Long, email: String): Result<Unit> = try {
        val response = apiService.shareShoppingListByEmail(listId, ShareByEmailRequest(email))
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSharedUsers(listId: Long): Result<List<User>> = try {
        val response = apiService.getSharedUsers(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun revokeShare(listId: Long, userId: Long): Result<Unit> = try {
        val response = apiService.revokeShare(listId, userId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun makeListPrivate(listId: Long): Result<Unit> {
        return try {
            val usersResponse = apiService.getSharedUsers(listId)
            if (!usersResponse.isSuccessful || usersResponse.body() == null) {
                Result.failure(Exception(usersResponse.message()))
            } else {
                val users = usersResponse.body()!!
                var failure: Exception? = null
                for (user in users) {
                    val revokeResponse = apiService.revokeShare(listId, user.id)
                    if (!revokeResponse.isSuccessful) {
                        failure = Exception(revokeResponse.message())
                        break
                    }
                }
                if (failure != null) {
                    Result.failure(failure)
                } else {
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListItems(listId: Long): Result<List<ListItem>> = try {
        val response = apiService.getListItems(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getListItemsCount(listId: Long): Result<Int> = try {
        val response = apiService.getListItems(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.pagination.total)
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
            Result.success(response.body()!!.item)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleItemPurchased(listId: Long, itemId: Long, currentPurchasedStatus: Boolean): Result<ListItem> = try {
        val request = ToggleItemPurchasedRequest(!currentPurchasedStatus)
        val response = apiService.toggleItemPurchased(listId, itemId, request)
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
