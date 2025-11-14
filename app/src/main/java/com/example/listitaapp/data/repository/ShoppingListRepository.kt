package com.example.listitaapp.data.repository

import com.example.listitaapp.data.api.ApiService
import com.example.listitaapp.data.dto.*
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.User
import com.example.listitaapp.data.model.ShoppingList
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    private fun <T> getErrorMessage(response: Response<T>, defaultMessage: String): String {
        return try {
            response.errorBody()?.string()?.let { errorBody ->
                val adapter = moshi.adapter(ErrorResponse::class.java)
                adapter.fromJson(errorBody)?.message
            } ?: response.message().ifEmpty { defaultMessage }
        } catch (e: Exception) {
            response.message().ifEmpty { defaultMessage }
        }
    }

    suspend fun getShoppingLists(): Result<List<ShoppingList>> = try {
        val response = apiService.getShoppingLists()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.data)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to get shopping lists")
            Result.failure(Exception(errorMessage))
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
            val errorMessage = getErrorMessage(response, "Failed to create list")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getShoppingListById(id: Long): Result<ShoppingList> = try {
        val response = apiService.getShoppingListById(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to get shopping list")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateShoppingListName(id: Long, name: String): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(name = name))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to update list name")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateShoppingListDescription(id: Long, description: String): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(description = description))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to update list description")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleShoppingListRecurring(id: Long, currentRecurring: Boolean): Result<ShoppingList> = try {
        val response = apiService.updateShoppingList(id, UpdateShoppingListRequest(recurring = !currentRecurring))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to toggle recurring status")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteShoppingList(id: Long): Result<Unit> = try {
        val response = apiService.deleteShoppingList(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to delete list")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun shareListWithEmail(listId: Long, email: String): Result<Unit> = try {
        val response = apiService.shareShoppingListByEmail(listId, ShareByEmailRequest(email))
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to share list")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSharedUsers(listId: Long): Result<List<User>> = try {
        val response = apiService.getSharedUsers(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to get shared users")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun revokeShare(listId: Long, userId: Long): Result<Unit> = try {
        val response = apiService.revokeShare(listId, userId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to revoke share")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun makeListPrivate(listId: Long): Result<Unit> {
        return try {
            val usersResponse = apiService.getSharedUsers(listId)
            if (!usersResponse.isSuccessful || usersResponse.body() == null) {
                val errorMessage = getErrorMessage(usersResponse, "Failed to get shared users")
                Result.failure(Exception(errorMessage))
            } else {
                val users = usersResponse.body()!!
                var failure: Exception? = null
                for (user in users) {
                    val revokeResponse = apiService.revokeShare(listId, user.id)
                    if (!revokeResponse.isSuccessful) {
                        val errorMessage = getErrorMessage(revokeResponse, "Failed to revoke share")
                        failure = Exception(errorMessage)
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
            val errorMessage = getErrorMessage(response, "Failed to get list items")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getListItemsCount(listId: Long): Result<Int> = try {
        val response = apiService.getListItems(listId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.pagination.total)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to get items count")
            Result.failure(Exception(errorMessage))
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
            val errorMessage = getErrorMessage(response, "Failed to add item")
            Result.failure(Exception(errorMessage))
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
            val errorMessage = getErrorMessage(response, "Failed to toggle item status")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteListItem(listId: Long, itemId: Long): Result<Unit> = try {
        val response = apiService.deleteListItem(listId, itemId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorMessage = getErrorMessage(response, "Failed to delete item")
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
