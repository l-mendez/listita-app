package com.example.listitaapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.listitaapp.data.model.Category
import kotlinx.coroutines.delay

@Composable
fun AddProductDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCreateProduct: (String, Long?) -> Unit,
    onCreateCategory: (String, (Category) -> Unit) -> Unit
) {
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var productName by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingCategoryId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(categories) {
        if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = null
        }
    }

    // Handle smooth transition back to product dialog after category creation
    LaunchedEffect(pendingCategoryId) {
        if (pendingCategoryId != null) {
            selectedCategoryId = pendingCategoryId
            // Small delay to ensure state updates complete and UI is ready
            delay(150)
            pendingCategoryId = null
            showCreateCategoryDialog = false
        }
    }

    // Use AnimatedVisibility for smooth transitions between dialogs
    AnimatedVisibility(
        visible = !showCreateCategoryDialog,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        )
    ) {
        CreateProductDialog(
            categories = categories,
            productName = productName,
            onProductNameChange = { productName = it },
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { selectedCategoryId = it },
            onDismiss = {
                productName = ""
                selectedCategoryId = null
                onDismiss()
            },
            onConfirm = { name, categoryId ->
                onCreateProduct(name, categoryId)
                productName = ""
                selectedCategoryId = null
            },
            onRequestCreateCategory = {
                // Reset any pending category to ensure clean state
                pendingCategoryId = null
                showCreateCategoryDialog = true
            }
        )
    }

    AnimatedVisibility(
        visible = showCreateCategoryDialog,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        )
    ) {
        CreateCategoryDialog(
            onDismiss = {
                // Only allow dismissal if category is not pending
                if (pendingCategoryId == null) {
                    showCreateCategoryDialog = false
                }
            },
            onCreate = { name ->
                onCreateCategory(name) { category ->
                    // Set pending category ID to trigger smooth transition
                    pendingCategoryId = category.id
                }
            },
            autoDismiss = false // We control dismissal timing for smooth transitions
        )
    }
}
