package com.example.listitaapp.data.repository

import com.example.listitaapp.data.datasource.ListItemRemoteDataSource
import com.example.listitaapp.data.datasource.ShoppingListRemoteDataSource
import com.example.listitaapp.data.mapper.toDomain
import com.example.listitaapp.domain.model.ListItem
import com.example.listitaapp.domain.model.ShoppingList
import com.example.listitaapp.domain.model.User
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val shoppingListRemoteDataSource: ShoppingListRemoteDataSource,
    private val listItemRemoteDataSource: ListItemRemoteDataSource
) {

    suspend fun getShoppingLists(
        name: String? = null,
        owner: Boolean? = null,
        recurring: Boolean? = null,
        page: Int = 1,
        perPage: Int = 10,
        sortBy: String = "name",
        order: String = "ASC"
    ): Result<List<ShoppingList>> = runCatching {
        val response = shoppingListRemoteDataSource.getShoppingLists(name, owner, recurring, page, perPage, sortBy, order)
        response.data.map { it.toDomain() }
    }

    suspend fun createShoppingList(name: String, description: String?, recurring: Boolean): Result<ShoppingList> =
        runCatching {
            shoppingListRemoteDataSource.createShoppingList(name, description, recurring).toDomain()
        }

    suspend fun getShoppingListById(id: Long): Result<ShoppingList> = runCatching {
        shoppingListRemoteDataSource.getShoppingListById(id).toDomain()
    }

    suspend fun updateShoppingListName(id: Long, name: String): Result<ShoppingList> = runCatching {
        shoppingListRemoteDataSource.updateShoppingListName(id, name).toDomain()
    }

    suspend fun updateShoppingListDescription(id: Long, description: String): Result<ShoppingList> = runCatching {
        shoppingListRemoteDataSource.updateShoppingListDescription(id, description).toDomain()
    }

    suspend fun toggleShoppingListRecurring(id: Long, currentRecurring: Boolean): Result<ShoppingList> = runCatching {
        shoppingListRemoteDataSource.toggleShoppingListRecurring(id, !currentRecurring).toDomain()
    }

    suspend fun deleteShoppingList(id: Long): Result<Unit> = runCatching {
        shoppingListRemoteDataSource.deleteShoppingList(id)
    }

    suspend fun shareListWithEmail(listId: Long, email: String): Result<Unit> = runCatching {
        shoppingListRemoteDataSource.shareListWithEmail(listId, email)
    }

    suspend fun getSharedUsers(listId: Long): Result<List<User>> = runCatching {
        shoppingListRemoteDataSource.getSharedUsers(listId).map { it.toDomain() }
    }

    suspend fun revokeShare(listId: Long, userId: Long): Result<Unit> = runCatching {
        shoppingListRemoteDataSource.revokeShare(listId, userId)
    }

    suspend fun makeListPrivate(listId: Long): Result<Unit> = runCatching {
        val sharedUsers = shoppingListRemoteDataSource.getSharedUsers(listId)
        sharedUsers.forEach { shoppingListRemoteDataSource.revokeShare(listId, it.id) }
    }

    suspend fun getListItems(listId: Long): Result<List<ListItem>> = runCatching {
        listItemRemoteDataSource.getListItems(listId).data.map { it.toDomain() }
    }

    suspend fun getListItemsCount(listId: Long): Result<Int> = runCatching {
        listItemRemoteDataSource.getListItemsCount(listId)
    }

    suspend fun addListItem(listId: Long, productId: Long, quantity: Double, unit: String): Result<ListItem> =
        runCatching {
            listItemRemoteDataSource.addListItem(listId, productId, quantity, unit).toDomain()
        }

    suspend fun updateListItem(
        listId: Long,
        itemId: Long,
        quantity: Double,
        unit: String,
        metadata: Map<String, String>? = null
    ): Result<ListItem> = runCatching {
        listItemRemoteDataSource.updateListItem(listId, itemId, quantity, unit, metadata).toDomain()
    }

    suspend fun toggleItemPurchased(listId: Long, itemId: Long, currentPurchasedStatus: Boolean): Result<ListItem> =
        runCatching {
            listItemRemoteDataSource.toggleItemPurchased(listId, itemId, !currentPurchasedStatus).toDomain()
        }

    suspend fun deleteListItem(listId: Long, itemId: Long): Result<Unit> = runCatching {
        listItemRemoteDataSource.deleteListItem(listId, itemId)
    }

    suspend fun purchaseList(listId: Long): Result<Unit> = runCatching {
        shoppingListRemoteDataSource.purchaseList(listId)
    }
}
