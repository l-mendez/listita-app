package com.example.listitaapp.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val recentlyCreatedProduct: Product? = null
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

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState().collect { isAuthenticated ->
                if (isAuthenticated) {
                    loadProducts()
                } else {
                    clearAllData()
                }
            }
        }
    }

    private fun clearAllData() {
        _uiState.update { ProductUiState() }
    }

    fun loadProducts(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val trimmedQuery = query?.takeIf { it.isNotBlank() }
            repository.getProducts(name = trimmedQuery).fold(
                onSuccess = { products ->
                    _uiState.update {
                        it.copy(products = products, isLoading = false)
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

    fun createProduct(name: String, categoryId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.createProduct(name, categoryId).fold(
                onSuccess = { product ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Product created successfully",
                            recentlyCreatedProduct = product
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

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchProducts() {
        val query = _uiState.value.searchQuery.trim()
        loadProducts(query.takeIf { it.isNotEmpty() })
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearRecentlyCreatedProduct() {
        _uiState.update { it.copy(recentlyCreatedProduct = null) }
    }
}
