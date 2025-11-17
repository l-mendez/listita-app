package com.example.listitaapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.listitaapp.domain.model.Category
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

    LaunchedEffect(pendingCategoryId) {
        if (pendingCategoryId != null) {
            selectedCategoryId = pendingCategoryId
            delay(150)
            pendingCategoryId = null
            showCreateCategoryDialog = false
        }
    }

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
                if (pendingCategoryId == null) {
                    showCreateCategoryDialog = false
                }
            },
            onCreate = { name ->
                onCreateCategory(name) { category ->
                    pendingCategoryId = category.id
                }
            },
            autoDismiss = false
        )
    }
}
