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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.data.model.ListItem
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.AppFab
import com.example.listitaapp.ui.components.AppConfirmDialog
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppFormDialog
import com.example.listitaapp.ui.components.AppSnackbarHost
import com.example.listitaapp.ui.components.rememberAppSnackbarState
import com.example.listitaapp.ui.components.appSnackTypeFromMessage
import com.example.listitaapp.ui.components.show
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.OptionsPopupMenu
import com.example.listitaapp.ui.components.PopupMenuAction
import com.example.listitaapp.ui.components.PopupHeaderButton
import com.example.listitaapp.ui.components.PopupHeaderDeleteButton
import com.example.listitaapp.ui.components.AppTextButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: Long,
    uiState: ShoppingListUiState,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onToggleItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onUpdateListName: (Long, String) -> Unit,
    onUpdateListDescription: (Long, String) -> Unit,
    onDeleteList: (Long) -> Unit,
    onToggleRecurring: (Long, Boolean) -> Unit,
    onShareByEmail: (Long, String) -> Unit,
    onLoadSharedUsers: (Long) -> Unit,
    onRevokeShare: (Long, Long) -> Unit,
    onMakePrivate: (Long) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showDeleteDialog by remember { mutableStateOf<ListItem?>(null) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var showEditListDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }

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

    // Delete item confirmation dialog
    showDeleteDialog?.let { item ->
        val productName = item.product?.name ?: stringResource(R.string.this_item)
        AppConfirmDialog(
            message = stringResource(R.string.confirm_remove_item_from_list, productName),
            onConfirm = {
                onDeleteItem(item.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null },
            destructive = true
        )
    }

    // Delete list confirmation dialog
    if (showDeleteListDialog) {
        AppConfirmDialog(
            message = stringResource(R.string.confirm_delete_list),
            onConfirm = {
                onDeleteList(listId)
                showDeleteListDialog = false
                onBack() // Navigate back after deleting list
            },
            onDismiss = { showDeleteListDialog = false },
            destructive = true
        )
    }

    // Edit list dialog (name + description)
    if (showEditListDialog && uiState.currentList != null) {
        var newName by remember { mutableStateOf(uiState.currentList!!.name) }
        var newDescription by remember { mutableStateOf(uiState.currentList!!.description ?: "") }
        AppFormDialog(
            title = stringResource(R.string.edit_list),
            onDismiss = { showEditListDialog = false },
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                if (newName != uiState.currentList!!.name) {
                    onUpdateListName(listId, newName.trim())
                }
                if (newDescription != (uiState.currentList!!.description ?: "")) {
                    onUpdateListDescription(listId, newDescription)
                }
                showEditListDialog = false
            },
            confirmEnabled = newName.isNotBlank(),
            icon = Icons.Filled.Edit
        ) {
            AppTextField(
                value = newName,
                onValueChange = { if (it.length <= 50) newName = it },
                label = stringResource(R.string.list_name)
            )
            AppTextField(
                value = newDescription,
                onValueChange = { if (it.length <= 200) newDescription = it },
                label = stringResource(R.string.list_description),
                singleLine = false,
                minLines = 2,
                maxLines = 4
            )
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.currentList?.name ?: "List",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var buttonPosition by remember { mutableStateOf<Offset?>(null) }
                    var buttonSize by remember { mutableStateOf<IntSize?>(null) }
                    IconButton(
                        onClick = {
                            buttonPosition?.let { pos ->
                                buttonSize?.let { size ->
                                    val bounds = Rect(
                                        left = pos.x,
                                        top = pos.y,
                                        right = pos.x + size.width,
                                        bottom = pos.y + size.height
                                    )
                                    anchorBounds = bounds
                                    showOptions = true
                                }
                            }
                        },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            buttonPosition = coordinates.localToRoot(Offset.Zero)
                            buttonSize = coordinates.size
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.configuracion))
                    }
                }
            )
        },
        floatingActionButton = {
            AppFab(onClick = onAddItem, modifier = Modifier.size(64.dp))
        },
        snackbarHost = { AppSnackbarHost(state = appSnackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Description (if available) - always show at top
            if (!uiState.currentList?.description.isNullOrBlank()) {
                StandardCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.currentList?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

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
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp // Extra bottom padding to avoid FAB overlap
                    ),
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

    // Options popup menu for the list
    OptionsPopupMenu(
        expanded = showOptions && uiState.currentList != null,
        onDismissRequest = { showOptions = false },
        anchorBounds = anchorBounds,
        headerButtons = {
            PopupHeaderButton(
                text = stringResource(R.string.recurrente),
                icon = Icons.Default.History,
                selected = uiState.currentList?.recurring == true,
                onClick = {
                    uiState.currentList?.let {
                        onToggleRecurring(it.id, it.recurring)
                        showOptions = false
                    }
                }
            )
            PopupHeaderDeleteButton(
                onClick = {
                    showDeleteListDialog = true
                    showOptions = false
                }
            )
        },
        actions = listOf(
            PopupMenuAction(
                text = stringResource(R.string.edit),
                icon = Icons.Default.Edit,
                onClick = {
                    showEditListDialog = true
                }
            ),
            PopupMenuAction(
                text = stringResource(R.string.hacer_privada),
                icon = Icons.Default.Lock,
                onClick = {
                    uiState.currentList?.let {
                        onMakePrivate(it.id)
                    }
                }
            ),
            PopupMenuAction(
                text = stringResource(R.string.compartir),
                icon = Icons.Default.Share,
                onClick = {
                    uiState.currentList?.let {
                        onLoadSharedUsers(it.id)
                        showShareDialog = true
                    }
                }
            )
        )
    )

    // Share dialog
    if (showShareDialog && uiState.currentList != null) {
        ShareListDialog(
            sharedUsers = uiState.sharedUsers,
            isLoading = uiState.isLoading,
            onDismiss = {
                showShareDialog = false
                showOptions = false
            },
            onShare = { email ->
                onShareByEmail(listId, email)
            },
            onRevoke = { userId ->
                onRevokeShare(listId, userId)
            },
            onMakePrivate = {
                onMakePrivate(listId)
            }
        )
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
    recentProduct: Product?,
    onCreateNewProduct: () -> Unit,
    onClearRecentProduct: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (Long, Double, String) -> Unit
) {
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("units") }
    var expanded by remember { mutableStateOf(false) }

    val commonUnits = listOf("units", "kg", "g", "l", "ml", "pcs")

    LaunchedEffect(recentProduct?.id) {
        if (recentProduct != null) {
            selectedProductId = recentProduct.id
            onClearRecentProduct()
        }
    }

    AppFormDialog(
        title = stringResource(R.string.add_item),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.add_item),
        confirmEnabled = selectedProductId != null && quantity.toDoubleOrNull() != null,
        onConfirm = {
            selectedProductId?.let { productId ->
                val qty = quantity.toDoubleOrNull() ?: 1.0
                onAdd(productId, qty, unit)
                onDismiss()
            }
        }
    ) {
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
            AppTextField(
                value = products.find { it.id == selectedProductId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.product_name),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = products.isNotEmpty(),
                modifier = Modifier.menuAnchor()
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
            AppTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = stringResource(R.string.quantity),
                modifier = Modifier.weight(1f)
            )

            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                AppTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = stringResource(R.string.unit),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor()
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

        TextButton(
            onClick = onCreateNewProduct,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(R.string.create_new_product))
        }
    }
}
