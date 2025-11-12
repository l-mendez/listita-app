package com.example.listitaapp.ui.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.AppExtendedFab

/**
 * Shopping List Detail Screen - Following HCI Principles:
 * - Visibility of system status
 * - User control and freedom (back navigation, delete items)
 * - Consistency and standards (checkboxes for completion)
 * - Error prevention
 * - Recognition over recall
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: Long,
    uiState: ShoppingListUiState,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onToggleItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<ListItem?>(null) }

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
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.confirm)) },
            text = { Text("Remove ${item.product?.name ?: "this item"} from list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteItem(item.id)
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.currentList?.name ?: "Shopping List",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {}
            )
        },
        floatingActionButton = {
            AppExtendedFab(
                onClick = onAddItem,
                text = stringResource(R.string.add_item)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.currentListItems.isEmpty()) {
                // Empty state
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
                            text = "No items in this list",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap + to add products",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                // Items list with progress summary
                Column {
                    // Progress summary card
                    val purchasedCount = uiState.currentListItems.count { it.purchased }
                    val totalCount = uiState.currentListItems.size

                    StandardCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Progress: $purchasedCount / $totalCount items",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinearProgressIndicator(
                                progress = { if (totalCount > 0) purchasedCount.toFloat() / totalCount else 0f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Items list
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.currentListItems, key = { it.id }) { item ->
                            ListItemCard(
                                item = item,
                                onToggle = { onToggleItem(item.id) },
                                onDelete = { showDeleteDialog = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListItemCard(
    item: ListItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
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
            Checkbox(
                checked = item.purchased,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface,
                    checkmarkColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else null,
                    color = if (item.purchased) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${item.quantity} ${item.unit}" +
                           (item.product?.category?.name?.let { " • $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToListDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onAdd: (Long, Double, String) -> Unit
) {
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("units") }
    var expanded by remember { mutableStateOf(false) }

    val commonUnits = listOf("units", "kg", "g", "l", "ml", "pcs")

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_item),
                    style = MaterialTheme.typography.headlineSmall
                )

                // Show message if no products available
                if (products.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "No products available. Please create products first in the Products screen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Product selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = if (products.isNotEmpty()) it else false }
                ) {
                    OutlinedTextField(
                        value = products.find { it.id == selectedProductId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.product_name)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        enabled = products.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(product.name)
                                        product.category?.let { category ->
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedProductId = product.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.quantity)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text(stringResource(R.string.unit)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            commonUnits.forEach { commonUnit ->
                                DropdownMenuItem(
                                    text = { Text(commonUnit) },
                                    onClick = {
                                        unit = commonUnit
                                        unitExpanded = false
                                    }
                                )
                            }
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
                            selectedProductId?.let { productId ->
                                val qty = quantity.toDoubleOrNull() ?: 1.0
                                onAdd(productId, qty, unit)
                                onDismiss()
                            }
                        },
                        enabled = selectedProductId != null && quantity.toDoubleOrNull() != null
                    ) {
                        Text(stringResource(R.string.add_item))
                    }
                }
            }
        }
    }
}
