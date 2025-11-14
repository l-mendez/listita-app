package com.example.listitaapp.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.model.Category
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

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
                    loadProducts()
                    loadCategories()
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
            ProductUiState(
                products = emptyList(),
                categories = emptyList(),
                isLoading = false,
                error = null,
                successMessage = null,
                searchQuery = ""
            )
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getProducts().fold(
                onSuccess = { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load products"
                        )
                    }
                }
            )
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().fold(
                onSuccess = { categories ->
                    _uiState.update { it.copy(categories = categories) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to load categories")
                    }
                }
            )
        }
    }

    fun createProduct(name: String, categoryId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.createProduct(name, categoryId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Product created successfully"
                        )
                    }
                    loadProducts()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create product"
                        )
                    }
                }
            )
        }
    }

    fun updateProduct(
        id: Long,
        name: String?,
        categoryId: Long?,
        metadata: Map<String, Any>?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.updateProduct(id, name, categoryId, metadata).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Product updated"
                        )
                    }
                    loadProducts()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update product"
                        )
                    }
                }
            )
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.deleteProduct(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Product deleted"
                        )
                    }
                    loadProducts()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to delete product"
                        )
                    }
                }
            )
        }
    }

    fun createCategory(name: String, onCreated: ((Category) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.createCategory(name).fold(
                onSuccess = { category ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = (it.categories + category).distinctBy { item -> item.id },
                            successMessage = "Category created successfully"
                        )
                    }
                    onCreated?.invoke(category)
                    loadCategories()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create category"
                        )
                    }
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredProducts(): List<Product> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.products
        } else {
            _uiState.value.products.filter {
                it.name.lowercase().contains(query) ||
                it.category?.name?.lowercase()?.contains(query) == true
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
