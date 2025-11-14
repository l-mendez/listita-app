package com.example.listitaapp.ui.components

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.listitaapp.data.model.Category

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

    LaunchedEffect(categories) {
        if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = null
        }
    }

    if (!showCreateCategoryDialog) {
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
            onCreate = { name, categoryId ->
                onCreateProduct(name, categoryId)
                productName = ""
                selectedCategoryId = null
            },
            onRequestCreateCategory = {
                showCreateCategoryDialog = true
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { name ->
                onCreateCategory(name) { category ->
                    selectedCategoryId = category.id
                }
            }
        )
    }
}
