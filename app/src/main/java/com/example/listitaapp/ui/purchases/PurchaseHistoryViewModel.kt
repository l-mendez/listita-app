package com.example.listitaapp.ui.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listitaapp.R
import com.example.listitaapp.domain.model.Purchase
import com.example.listitaapp.data.repository.PurchaseRepository
import com.example.listitaapp.ui.common.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseHistoryUiState(
    val purchases: List<Purchase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: UiMessage? = null
)

@HiltViewModel
class PurchaseHistoryViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState())
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPurchaseHistory()
    }

    fun loadPurchaseHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            purchaseRepository.getPurchases().fold(
                onSuccess = { purchases ->
                    _uiState.update {
                        it.copy(
                            purchases = purchases,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load purchase history"
                        )
                    }
                }
            )
        }
    }

    fun restorePurchase(purchaseId: Long, shoppingListViewModel: com.example.listitaapp.ui.lists.ShoppingListViewModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            purchaseRepository.restorePurchase(purchaseId).fold(
                onSuccess = { shoppingList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = UiMessage(resId = R.string.list_restored)
                        )
                    }
                    // Add the restored list to the shopping lists state
                    shoppingListViewModel.addListToListState(shoppingList)
                    // Reload history to refresh the list
                    loadPurchaseHistory()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to restore purchase"
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
