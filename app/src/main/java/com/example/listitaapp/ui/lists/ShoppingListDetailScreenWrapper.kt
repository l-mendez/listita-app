package com.example.listitaapp.ui.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.listitaapp.ui.categories.CategoryViewModel
import com.example.listitaapp.ui.components.AddProductDialog
import com.example.listitaapp.ui.products.ProductViewModel

@Composable
fun ShoppingListDetailScreenWrapper(
    listId: Long,
    viewModel: ShoppingListViewModel,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    var addItemDialogState by remember { mutableStateOf(AddItemDialogState.None) }
    var resumeAddItemAfterProduct by remember { mutableStateOf(false) }

    LaunchedEffect(listId) {
        viewModel.loadListDetails(listId)
    }

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.clearNavigateBack()
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentList()
        }
    }

    ShoppingListDetailScreen(
        listId = listId,
        uiState = uiState,
        onBack = onBack,
        onAddItem = {
            resumeAddItemAfterProduct = false
            addItemDialogState = AddItemDialogState.AddItem
        },
        onToggleItem = { itemId ->
            viewModel.toggleItemPurchased(listId, itemId)
        },
        onDeleteItem = { itemId ->
            viewModel.deleteListItem(listId, itemId)
        },
        onClearError = { viewModel.clearError() },
        onClearSuccess = { viewModel.clearSuccess() }
    )

    AnimatedVisibility(
        visible = addItemDialogState == AddItemDialogState.AddItem,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        )
    ) {
        AddItemToListDialog(
            products = productUiState.products,
            recentProduct = productUiState.recentlyCreatedProduct,
            onCreateNewProduct = {
                productViewModel.loadProducts()
                categoryViewModel.loadCategories()
                resumeAddItemAfterProduct = true
                addItemDialogState = AddItemDialogState.CreateProduct
            },
            onClearRecentProduct = { productViewModel.clearRecentlyCreatedProduct() },
            onDismiss = { addItemDialogState = AddItemDialogState.None },
            onAdd = { productId, quantity, unit ->
                viewModel.addItemToList(listId, productId, quantity, unit)
                addItemDialogState = AddItemDialogState.None
            }
        )
    }

    AnimatedVisibility(
        visible = addItemDialogState == AddItemDialogState.CreateProduct,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        )
    ) {
        AddProductDialog(
            categories = categoryUiState.categories,
            onDismiss = {
                addItemDialogState = if (resumeAddItemAfterProduct) {
                    resumeAddItemAfterProduct = false
                    AddItemDialogState.AddItem
                } else {
                    AddItemDialogState.None
                }
            },
            onCreateProduct = { name, categoryId ->
                productViewModel.createProduct(name, categoryId)
                addItemDialogState = if (resumeAddItemAfterProduct) {
                    resumeAddItemAfterProduct = false
                    AddItemDialogState.AddItem
                } else {
                    AddItemDialogState.None
                }
            },
            onCreateCategory = { name, onCreated ->
                categoryViewModel.createCategory(name) { category ->
                    onCreated(category)
                }
            }
        )
    }
}

private enum class AddItemDialogState {
    None,
    AddItem,
    CreateProduct
}
