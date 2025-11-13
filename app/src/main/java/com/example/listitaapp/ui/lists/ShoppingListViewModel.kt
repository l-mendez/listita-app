package com.example.listitaapp.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.data.model.ShoppingList
import com.example.listitaapp.data.repository.ProductRepository
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
    val availableProducts: List<Product> = emptyList(),
    val itemsCountByListId: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val listRepository: ShoppingListRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadShoppingLists()
        loadProducts()
    }

    fun loadShoppingLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            listRepository.getShoppingLists().fold(
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

    fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().fold(
                onSuccess = { products ->
                    _uiState.update { it.copy(availableProducts = products) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to load products")
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
                    _uiState.update { it.copy(successMessage = "Lista actualizada") }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "No se pudo actualizar") }
                }
            )
        }
    }

    fun updateListDescription(id: Long, description: String) {
        viewModelScope.launch {
            listRepository.updateShoppingListDescription(id, description).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Lista actualizada") }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "No se pudo actualizar") }
                }
            )
        }
    }

    fun toggleListRecurring(id: Long, currentRecurring: Boolean) {
        viewModelScope.launch {
            listRepository.toggleShoppingListRecurring(id, currentRecurring).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Lista actualizada") }
                    loadShoppingLists()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "No se pudo actualizar") }
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

            listRepository.toggleItemPurchased(listId, itemId, currentItem.purchased).fold(
                onSuccess = {
                    loadListDetails(listId)
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
