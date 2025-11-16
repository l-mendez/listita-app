package com.example.listitaapp.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.R
import com.example.listitaapp.domain.model.Product
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.data.repository.ProductRepository
import com.example.listitaapp.ui.common.UiMessage
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
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val error: String? = null,
    val successMessage: UiMessage? = null,
    val searchQuery: String = "",
    val recentlyCreatedProduct: Product? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val pageSize = 10
    private var appliedQuery: String? = null

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
        appliedQuery = null
        _uiState.update { ProductUiState() }
    }

    fun loadProducts(query: String? = null) {
        viewModelScope.launch {
            val trimmedQuery = query?.trim()?.takeIf { it.isNotEmpty() }
            if (query != null) {
                appliedQuery = trimmedQuery
            }
            val effectiveQuery = trimmedQuery ?: appliedQuery

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    currentPage = 1,
                    hasNextPage = false,
                    isLoadingMore = false
                )
            }

            repository.getProducts(name = effectiveQuery, page = 1, perPage = pageSize).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            products = result.data,
                            isLoading = false,
                            currentPage = result.pagination.page,
                            hasNextPage = result.pagination.hasNext
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

    fun loadMoreProducts() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || !state.hasNextPage) return

        viewModelScope.launch {
            val nextPage = state.currentPage + 1
            val query = appliedQuery

            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            repository.getProducts(name = query, page = nextPage, perPage = pageSize).fold(
                onSuccess = { result ->
                    _uiState.update { current ->
                        current.copy(
                            products = current.products + result.data,
                            isLoadingMore = false,
                            currentPage = result.pagination.page,
                            hasNextPage = result.pagination.hasNext
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
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
                            successMessage = UiMessage(resId = R.string.product_created),
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
        categoryId: Long?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.updateProduct(id, name, categoryId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = UiMessage(resId = R.string.product_updated)
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
                            successMessage = UiMessage(resId = R.string.product_deleted)
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
