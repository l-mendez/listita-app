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
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Success snackbar
    if (uiState.successMessage != null) {
        LaunchedEffect(uiState.successMessage) {
            kotlinx.coroutines.delay(2000)
            onClearSuccess()
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.confirm)) },
            text = { Text(stringResource(R.string.confirm_delete_product)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(product.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
            },
            onDelete = {
                showDeleteDialog = product
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
            ExtendedFloatingActionButton(
                onClick = onCreateProduct,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.create_product)) }
            )
        },
        snackbarHost = {
            if (uiState.successMessage != null) {
                Snackbar {
                    Text(uiState.successMessage)
                }
            }
        }
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
                    onEditClick = { editingProduct = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductDialog(
    product: Product,
    categories: List<com.example.listitaapp.data.model.Category>,
    onDismiss: () -> Unit,
    onApply: (name: String?, price: String?, categoryId: Long?) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.metadata?.get("price")?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product.category?.id) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        StandardCard {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit product",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categories") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            onApply(name.ifBlank { null }, price.ifBlank { null }, selectedCategoryId)
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onEditClick: (Product) -> Unit,
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
                onEditClick = { onEditClick(product) }
            )
        }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onEditClick: () -> Unit
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

            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductDialog(
    categories: List<com.example.listitaapp.data.model.Category>,
    onDismiss: () -> Unit,
    onCreate: (String, Long?) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_product),
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                onCreate(productName, selectedCategoryId)
                                onDismiss()
                            }
                        },
                        enabled = productName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.create_product))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_category)) },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text(stringResource(R.string.category_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onCreate(categoryName)
                        onDismiss()
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text(stringResource(R.string.create_category))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
