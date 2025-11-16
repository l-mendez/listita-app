package com.example.listitaapp.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.R
import com.example.listitaapp.domain.model.ListItem
import com.example.listitaapp.domain.model.ShoppingList
import com.example.listitaapp.domain.model.User
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.data.repository.ShoppingListRepository
import com.example.listitaapp.ui.common.UiMessage
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
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val itemsCurrentPage: Int = 1,
    val itemsHasNextPage: Boolean = false,
    val isItemsLoadingMore: Boolean = false,
    val error: String? = null,
    val successMessage: UiMessage? = null,
    val shouldNavigateBack: Boolean = false
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val listRepository: ShoppingListRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val pageSize = 10
    private val listItemsPageSize = 10
    private var appliedSearchQuery: String? = null

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState().collect { isAuthenticated ->
                if (isAuthenticated) {
                    loadShoppingLists()
                } else {
                    clearAllData()
                }
            }
        }
    }

    private fun clearAllData() {
        appliedSearchQuery = null
        _uiState.update {
            ShoppingListUiState(
                lists = emptyList(),
                currentList = null,
                currentListItems = emptyList(),
                itemsCountByListId = emptyMap(),
                sharedUsers = emptyList(),
                isLoading = false,
                isItemsLoadingMore = false,
                error = null,
                successMessage = null
            )
        }
    }

    fun loadShoppingLists(query: String? = null) {
        viewModelScope.launch {
            val providedQuery = query?.trim()
            val sanitizedQuery = providedQuery?.takeIf { it.isNotEmpty() }
            if (query != null) {
                appliedSearchQuery = sanitizedQuery
            }
            val effectiveQuery = sanitizedQuery ?: appliedSearchQuery

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    itemsCountByListId = emptyMap(),
                    currentPage = 1,
                    hasNextPage = false,
                    isLoadingMore = false
                )
            }

            listRepository.getShoppingLists(name = effectiveQuery, page = 1, perPage = pageSize).fold(
                onSuccess = { result ->
                    val lists = result.data
                    _uiState.update {
                        it.copy(
                            lists = lists,
                            isLoading = false,
                            currentPage = result.pagination.page,
                            hasNextPage = result.pagination.hasNext
                        )
                    }
                    fetchItemsCountForLists(lists, resetExisting = true)
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

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || !state.hasNextPage) return

        viewModelScope.launch {
            val nextPage = state.currentPage + 1
            val query = appliedSearchQuery
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            listRepository.getShoppingLists(name = query, page = nextPage, perPage = pageSize).fold(
                onSuccess = { result ->
                    val newLists = result.data
                    _uiState.update { current ->
                        current.copy(
                            lists = current.lists + newLists,
                            isLoadingMore = false,
                            currentPage = result.pagination.page,
                            hasNextPage = result.pagination.hasNext
                        )
                    }
                    fetchItemsCountForLists(newLists, resetExisting = false)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = exception.message ?: "Failed to load lists"
                        )
                    }
                }
            )
        }
    }

    fun loadMoreListItems(listId: Long) {
        val state = _uiState.value
        if (state.isItemsLoadingMore || state.isLoading || !state.itemsHasNextPage) return

        viewModelScope.launch {
            val nextPage = state.itemsCurrentPage + 1
            _uiState.update { it.copy(isItemsLoadingMore = true, error = null) }

            listRepository.getListItems(listId, page = nextPage, perPage = listItemsPageSize).fold(
                onSuccess = { result ->
                    val newItems = result.data.filter { it.product != null }
                    _uiState.update { current ->
                        current.copy(
                            currentListItems = current.currentListItems + newItems,
                            isItemsLoadingMore = false,
                            itemsCurrentPage = result.pagination.page,
                            itemsHasNextPage = result.pagination.hasNext,
                            itemsCountByListId = current.itemsCountByListId + (listId to result.pagination.total)
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isItemsLoadingMore = false,
                            error = exception.message ?: "Failed to load items"
                        )
                    }
                }
            )
        }
    }

    private fun fetchItemsCountForLists(lists: List<ShoppingList>, resetExisting: Boolean) {
        viewModelScope.launch {
            var counts = if (resetExisting) emptyMap<Long, Int>() else _uiState.value.itemsCountByListId
            lists.forEach { list ->
                listRepository.getListItemsCount(list.id).fold(
                    onSuccess = { count ->
                        counts = counts + (list.id to count)
                        _uiState.update { it.copy(itemsCountByListId = counts) }
                    },
                    onFailure = { _ -> }
                )
            }
        }
    }

    fun loadListDetails(listId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isItemsLoadingMore = false,
                    itemsCurrentPage = 1,
                    itemsHasNextPage = false
                )
            }

            val listResult = listRepository.getShoppingListById(listId)
            val itemsResult = listRepository.getListItems(listId, page = 1, perPage = listItemsPageSize)

            listResult.fold(
                onSuccess = { list ->
                    itemsResult.fold(
                        onSuccess = { paginated ->
                            val validItems = paginated.data.filter { it.product != null }

                            _uiState.update {
                                it.copy(
                                    currentList = list,
                                    currentListItems = validItems,
                                    isLoading = false,
                                    itemsCurrentPage = paginated.pagination.page,
                                    itemsHasNextPage = paginated.pagination.hasNext,
                                    itemsCountByListId = it.itemsCountByListId + (list.id to paginated.pagination.total)
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
                            successMessage = UiMessage(resId = R.string.list_created)
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
                            successMessage = UiMessage(resId = R.string.list_deleted)
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
                    _uiState.update { it.copy(successMessage = UiMessage(resId = R.string.list_updated)) }
                    loadShoppingLists()
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
                    _uiState.update { it.copy(successMessage = UiMessage(resId = R.string.list_updated)) }
                    loadShoppingLists()
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
                    _uiState.update { it.copy(successMessage = UiMessage(resId = R.string.list_updated)) }
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
                            successMessage = UiMessage(resId = R.string.item_added)
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

    fun updateListItem(listId: Long, itemId: Long, quantity: Double, unit: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentItem = _uiState.value.currentListItems.find { it.id == itemId }
            val metadata = currentItem?.metadata

            listRepository.updateListItem(listId, itemId, quantity, unit, metadata).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = UiMessage(resId = R.string.item_updated)
                        )
                    }
                    loadListDetails(listId)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to update item")
                    }
                }
            )
        }
    }

    fun toggleItemPurchased(listId: Long, itemId: Long) {
        viewModelScope.launch {
            val currentItem = _uiState.value.currentListItems.find { it.id == itemId }
            if (currentItem == null) {
                _uiState.update {
                    it.copy(error = "Item not found")
                }
                return@launch
            }

            val currentList = _uiState.value.currentList
            val currentItems = _uiState.value.currentListItems
            val willBeCompleted = if (currentList != null && !currentList.recurring) {
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
                        _uiState.update {
                            it.copy(
                                currentList = null,
                                currentListItems = emptyList(),
                                shouldNavigateBack = true
                            )
                        }

                        viewModelScope.launch {
                            kotlinx.coroutines.delay(100)
                            listRepository.purchaseList(listId).fold(
                                onSuccess = {
                                    _uiState.update {
                                        it.copy(successMessage = UiMessage(resId = R.string.list_completed_history))
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
                    _uiState.update { it.copy(successMessage = UiMessage(resId = R.string.item_removed)) }
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
            it.copy(
                currentList = null,
                currentListItems = emptyList(),
                itemsCurrentPage = 1,
                itemsHasNextPage = false,
                isItemsLoadingMore = false
            )
        }
    }

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
                        it.copy(isLoading = false, successMessage = UiMessage(resId = R.string.list_shared))
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
                    _uiState.update { it.copy(isLoading = false, successMessage = UiMessage(resId = R.string.access_revoked)) }
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
                    _uiState.update { it.copy(isLoading = false, successMessage = UiMessage(resId = R.string.list_set_private), sharedUsers = emptyList()) }
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
