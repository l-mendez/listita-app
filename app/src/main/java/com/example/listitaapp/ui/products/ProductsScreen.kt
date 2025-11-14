package com.example.listitaapp.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.OptionsBottomSheet
import com.example.listitaapp.ui.components.SheetActionItem
import com.example.listitaapp.ui.components.SheetHeaderWithDelete
import com.example.listitaapp.ui.components.AppConfirmDialog
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppFormDialog
import com.example.listitaapp.ui.components.AppSnackbarHost
import com.example.listitaapp.ui.components.rememberAppSnackbarState
import com.example.listitaapp.ui.components.appSnackTypeFromMessage
import com.example.listitaapp.ui.components.show
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppSearchField
import com.example.listitaapp.ui.components.AppExtendedFab

/**
 * Products Screen - Following HCI Principles:
 * - Visibility of system status (loading, error states)
 * - User control and freedom (search, filter, delete)
 * - Recognition over recall (clear labels, icons)
 * - Flexibility and efficiency (search, quick actions)
 * - Aesthetic and minimalist design
 * - Error prevention and recovery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    uiState: ProductUiState,
    onCreateProduct: () -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onUpdateProduct: (Long, String?, String?, Long?) -> Unit,
    onCreateCategory: () -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    // Error dialog (standardized)
    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    // Success snackbar (standardized)
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            appSnackbar.show(message, appSnackTypeFromMessage(message))
            onClearSuccess()
        }
    }

    // Delete confirmation dialog (standardized)
    showDeleteDialog?.let { product ->
        AppConfirmDialog(
            message = stringResource(R.string.confirm_delete_product),
            onConfirm = {
                onDeleteProduct(product.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null },
            destructive = true
        )
    }

    // Edit product dialog
    editingProduct?.let { product ->
        EditProductDialog(
            product = product,
            categories = uiState.categories,
            onDismiss = { editingProduct = null },
            onApply = { name, price, categoryId ->
                onUpdateProduct(product.id, name, price, categoryId)
                editingProduct = null
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.products),
                actions = {
                    IconButton(onClick = onCreateCategory) {
                        Icon(Icons.Default.Add, contentDescription = "Create category")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            AppExtendedFab(
                onClick = onCreateProduct,
                text = stringResource(R.string.create_product),
                icon = Icons.Default.Add
            )
        },
        snackbarHost = { AppSnackbarHost(state = appSnackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar (HCI: Flexibility and efficiency)
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Loading indicator (HCI: Visibility of system status)
            if (uiState.isLoading && uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.products.isEmpty()) {
                // Empty state (HCI: Visibility of system status)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = stringResource(R.string.empty_products),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                // Products list (uses filtered products if search query exists)
                ProductsList(
                    products = if (uiState.searchQuery.isEmpty()) {
                        uiState.products
                    } else {
                        uiState.products.filter {
                            it.name.lowercase().contains(uiState.searchQuery.lowercase()) ||
                            it.category?.name?.lowercase()?.contains(uiState.searchQuery.lowercase()) == true
                        }
                    },
                    onSettingsClick = {
                        selectedProduct = it
                        showOptions = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Options bottom sheet for products
    if (showOptions && selectedProduct != null) {
        OptionsBottomSheet(
            onDismissRequest = { showOptions = false },
            headerContent = {
                SheetHeaderWithDelete(
                    onDeleteClick = {
                        showOptions = false
                        showDeleteDialog = selectedProduct
                    }
                )
            }
        ) {
            SheetActionItem(
                text = "Edit",
                icon = Icons.Default.Edit,
                onClick = {
                    editingProduct = selectedProduct
                    showOptions = false
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.search),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductDialog(
    product: Product,
    categories: List<com.example.listitaapp.data.model.Category>,
    onDismiss: () -> Unit,
    onApply: (name: String?, price: String?, categoryId: Long?) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.metadata?.get("price")?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product.category?.id) }
    var expanded by remember { mutableStateOf(false) }

    AppFormDialog(
        title = stringResource(R.string.edit),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.save),
        onConfirm = { onApply(name.ifBlank { null }, price.ifBlank { null }, selectedCategoryId) },
        confirmEnabled = name.isNotBlank()
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.product_name)
        )
        AppTextField(
            value = price,
            onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
            label = "Price"
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            AppTextField(
                value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.category),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategoryId = category.id
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onSettingsClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductItem(
                product = product,
                onSettingsClick = { onSettingsClick(product) }
            )
        }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onSettingsClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val price = product.metadata?.get("price")?.toString()
                    if (!price.isNullOrBlank()) {
                        AssistChip(onClick = {}, label = { Text("$$price") })
                    }
                    product.category?.let { category ->
                        AssistChip(onClick = {}, label = { Text(category.name) })
                    }
                }
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Settings"
                )
            }
        }
    }
}
