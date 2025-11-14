package com.example.listitaapp.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.data.model.Category
import com.example.listitaapp.data.repository.AuthRepository
import com.example.listitaapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState().collect { isAuthenticated ->
                if (isAuthenticated) {
                    loadCategories()
                } else {
                    clearAllData()
                }
            }
        }
    }

    private fun clearAllData() {
        _uiState.update { CategoryUiState() }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getCategories().fold(
                onSuccess = { categories ->
                    _uiState.update {
                        it.copy(categories = categories, isLoading = false)
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load categories"
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

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteCategory(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Category deleted"
                        )
                    }
                    loadCategories()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to delete category"
                        )
                    }
                }
            )
        }
    }

    fun updateCategory(id: Long, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.updateCategory(id, name).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Category updated"
                        )
                    }
                    loadCategories()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update category"
                        )
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
}
