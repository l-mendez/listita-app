package com.example.listitaapp.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.ShoppingList
import com.example.listitaapp.data.model.User
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val lists: List<ShoppingList> = emptyList(),
    val currentList: ShoppingList? = null,
    val currentListItems: List<ListItem> = emptyList(),
    val itemsCountByListId: Map<Long, Int> = emptyMap(),
    val sharedUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val shouldNavigateBack: Boolean = false
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val listRepository: ShoppingListRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    /**
     * Observe authentication state changes and reload/clear data accordingly
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState().collect { isAuthenticated ->
                if (isAuthenticated) {
                    // User logged in - load fresh data for the authenticated user
                    loadShoppingLists()
                } else {
                    // User logged out - clear all cached data
                    clearAllData()
                }
            }
        }
    }

    /**
     * Clear all cached data when user logs out
     */
    private fun clearAllData() {
        _uiState.update {
            ShoppingListUiState(
                lists = emptyList(),
                currentList = null,
                currentListItems = emptyList(),
                itemsCountByListId = emptyMap(),
                sharedUsers = emptyList(),
                isLoading = false,
                error = null,
                successMessage = null
            )
        }
    }

    fun loadShoppingLists(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val trimmedQuery = query?.takeIf { it.isNotBlank() }

            listRepository.getShoppingLists(name = trimmedQuery).fold(
                onSuccess = { lists ->
                    _uiState.update {
                        it.copy(
                            lists = lists,
                            isLoading = false
                        )
                    }
                    // Load item counts in parallel without blocking UI
                    viewModelScope.launch {
                        val countsMutable = mutableMapOf<Long, Int>()
                        lists.forEach { list ->
                            // Fire and collect sequentially to avoid too many parallel calls
                            listRepository.getListItemsCount(list.id).fold(
                                onSuccess = { count -> countsMutable[list.id] = count },
                                onFailure = { _ -> }
                            )
                            _uiState.update { state ->
                                state.copy(itemsCountByListId = state.itemsCountByListId + countsMutable)
                            }
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load lists"
                        )
                    }
                }
            )
        }
    }

    fun loadListDetails(listId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load list details and items
            val listResult = listRepository.getShoppingListById(listId)
            val itemsResult = listRepository.getListItems(listId)

            listResult.fold(
                onSuccess = { list ->
                    itemsResult.fold(
                        onSuccess = { items ->
                            // Filter out items with deleted products (product is null)
                            val validItems = items.filter { it.product != null }

                            _uiState.update {
                                it.copy(
                                    currentList = list,
                                    currentListItems = validItems,
                                    isLoading = false,
                                    // Keep lists screen counters in sync while user is on detail
                                    itemsCountByListId = it.itemsCountByListId + (list.id to validItems.size)
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update {
                                it.copy(
                                    currentList = list,
                                    isLoading = false,
                                    error = exception.message ?: "Failed to load items"
                                )
                            }
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load list"
                        )
                    }
                }
            )
        }
    }

    fun createShoppingList(name: String, description: String?, recurring: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            listRepository.createShoppingList(name, description, recurring).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "List created successfully"
                        )
                    }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create list"
                        )
                    }
                }
            )
        }
    }

    fun deleteShoppingList(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            listRepository.deleteShoppingList(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "List deleted"
                        )
                    }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to delete list"
                        )
                    }
                }
            )
        }
    }

    fun updateListName(id: Long, name: String) {
        viewModelScope.launch {
            listRepository.updateShoppingListName(id, name).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "List updated") }
                    loadShoppingLists()
                    // Reload current list details if viewing detail page
                    if (_uiState.value.currentList?.id == id) {
                        loadListDetails(id)
                    }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "Failed to update") }
                }
            )
        }
    }

    fun updateListDescription(id: Long, description: String) {
        viewModelScope.launch {
            listRepository.updateShoppingListDescription(id, description).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "List updated") }
                    loadShoppingLists()
                    // Reload current list details if viewing detail page
                    if (_uiState.value.currentList?.id == id) {
                        loadListDetails(id)
                    }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "Failed to update") }
                }
            )
        }
    }

    fun toggleListRecurring(id: Long, currentRecurring: Boolean) {
        viewModelScope.launch {
            listRepository.toggleShoppingListRecurring(id, currentRecurring).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "List updated") }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "Failed to update") }
                }
            )
        }
    }

    fun addItemToList(listId: Long, productId: Long, quantity: Double, unit: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            listRepository.addListItem(listId, productId, quantity, unit).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Item added to list"
                        )
                    }
                    loadListDetails(listId)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to add item"
                        )
                    }
                }
            )
        }
    }

    fun toggleItemPurchased(listId: Long, itemId: Long) {
        viewModelScope.launch {
            // Find the current item to get its purchased status
            val currentItem = _uiState.value.currentListItems.find { it.id == itemId }
            if (currentItem == null) {
                _uiState.update {
                    it.copy(error = "Item not found")
                }
                return@launch
            }

            // Check if toggling this item will complete the list
            val currentList = _uiState.value.currentList
            val currentItems = _uiState.value.currentListItems
            val willBeCompleted = if (currentList != null && !currentList.recurring) {
                // After toggling, check if all items will be purchased
                val itemsAfterToggle = currentItems.map { item ->
                    if (item.id == itemId) !item.purchased else item.purchased
                }
                itemsAfterToggle.isNotEmpty() && itemsAfterToggle.all { it }
            } else {
                false
            }

            listRepository.toggleItemPurchased(listId, itemId, currentItem.purchased).fold(
                onSuccess = {
                    if (willBeCompleted) {
                        // Navigate back first, then complete the list
                        _uiState.update {
                            it.copy(
                                currentList = null,
                                currentListItems = emptyList(),
                                shouldNavigateBack = true
                            )
                        }

                        // Then move to history (after navigation happens)
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(100) // Small delay to ensure navigation completes
                            listRepository.purchaseList(listId).fold(
                                onSuccess = {
                                    _uiState.update {
                                        it.copy(successMessage = "List completed and moved to history!")
                                    }
                                    loadShoppingLists()
                                },
                                onFailure = { exception ->
                                    _uiState.update {
                                        it.copy(error = exception.message ?: "Failed to complete list")
                                    }
                                }
                            )
                        }
                    } else {
                        // Normal case: just reload list details
                        loadListDetails(listId)
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update item")
                    }
                }
            )
        }
    }

    fun deleteListItem(listId: Long, itemId: Long) {
        viewModelScope.launch {
            listRepository.deleteListItem(listId, itemId).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Item removed") }
                    loadListDetails(listId)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to remove item")
                    }
                }
            )
        }
    }

    fun clearCurrentList() {
        _uiState.update {
            it.copy(currentList = null, currentListItems = emptyList())
        }
    }

    // ====== Sharing / Privacy ======
    fun loadSharedUsers(listId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            listRepository.getSharedUsers(listId).fold(
                onSuccess = { users ->
                    _uiState.update { state ->
                        state.copy(sharedUsers = users, isLoading = false)
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to load shared users")
                    }
                }
            )
        }
    }

    fun shareListByEmail(listId: Long, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            listRepository.shareListWithEmail(listId, email).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "List shared")
                    }
                    loadSharedUsers(listId)
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to share list")
                    }
                }
            )
        }
    }

    fun revokeUserAccess(listId: Long, userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            listRepository.revokeShare(listId, userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Access revoked") }
                    loadSharedUsers(listId)
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to revoke access")
                    }
                }
            )
        }
    }

    fun makeListPrivate(listId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            listRepository.makeListPrivate(listId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "List set to private", sharedUsers = emptyList()) }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to make list private")
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearNavigateBack() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    fun addListToListState(list: ShoppingList) {
        _uiState.update { state ->
            val updatedLists = if (state.lists.any { it.id == list.id }) {
                state.lists.map { if (it.id == list.id) list else it }
            } else {
                state.lists + list
            }
            state.copy(lists = updatedLists)
        }
        viewModelScope.launch {
            listRepository.getListItemsCount(list.id).fold(
                onSuccess = { count ->
                    _uiState.update { state ->
                        state.copy(itemsCountByListId = state.itemsCountByListId + (list.id to count))
                    }
                },
                onFailure = { _ -> }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchShoppingLists() {
        val query = _uiState.value.searchQuery.trim()
        loadShoppingLists(query.takeIf { it.isNotEmpty() })
    }

}
