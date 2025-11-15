package com.example.listitaapp.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.data.model.Product
import com.example.listitaapp.data.model.Category
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.OptionsPopupMenu
import com.example.listitaapp.ui.components.PopupMenuAction
import com.example.listitaapp.ui.components.PopupHeaderDeleteButton
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
import com.example.listitaapp.ui.components.AppSearchButton
import com.example.listitaapp.ui.components.AppFab
import com.example.listitaapp.ui.components.rememberWindowSize
import com.example.listitaapp.ui.components.WindowSizeClass
import com.example.listitaapp.ui.components.isLandscape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    uiState: ProductUiState,
    categories: List<Category>,
    onCreateProduct: () -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onUpdateProduct: (Long, String?, String?, Long?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }

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
            categories = categories,
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
                title = stringResource(R.string.products)
            )
        },
        floatingActionButton = {
            AppFab(onClick = onCreateProduct, modifier = Modifier.size(64.dp))
        },
        snackbarHost = { AppSnackbarHost(state = appSnackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            val windowSize = rememberWindowSize()
            val landscape = isLandscape()

            // Calculate responsive padding based on screen size
            val horizontalPadding = when {
                windowSize.width == WindowSizeClass.Compact -> 16.dp
                landscape -> 120.dp
                else -> 90.dp // Tablet portrait
            }

            if (windowSize.width == WindowSizeClass.Compact && !landscape) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontalPadding)
                )
            }

            // Loading indicator
            if (uiState.isLoading && uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.products.isEmpty()) {
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
                            text = stringResource(R.string.empty_products),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                // Products list (uses filtered products if search query exists)
                ProductsList(
                    products = uiState.products,
                    onSettingsClick = { product, bounds ->
                        selectedProduct = product
                        anchorBounds = bounds
                        showOptions = true
                    },
                    horizontalPadding = horizontalPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Search button overlay for tablet/landscape mode (positioned above FAB)
    val windowSize = rememberWindowSize()
    val landscape = isLandscape()
    if (windowSize.width != WindowSizeClass.Compact || landscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 148.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AppSearchButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier.size(64.dp)
            )
        }
    }

    // Search dialog for tablet/landscape mode
    if (showSearchDialog) {
        AppFormDialog(
            title = stringResource(R.string.search),
            onDismiss = { showSearchDialog = false },
            confirmLabel = stringResource(R.string.ok),
            onConfirm = { showSearchDialog = false },
            confirmEnabled = true
        ) {
            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = stringResource(R.string.search)
            )
        }
    }

    // Options popup menu for products
    OptionsPopupMenu(
        expanded = showOptions && selectedProduct != null,
        onDismissRequest = { showOptions = false },
        anchorBounds = anchorBounds,
        headerButtons = null, // No header buttons for products
        actions = listOf(
            PopupMenuAction(
                text = stringResource(R.string.edit),
                icon = Icons.Default.Edit,
                onClick = {
                    editingProduct = selectedProduct
                }
            ),
            PopupMenuAction(
                text = stringResource(R.string.delete),
                icon = Icons.Default.Delete,
                onClick = {
                    showDeleteDialog = selectedProduct
                },
                destructive = true
            )
        )
    )
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var hadFocus by remember { mutableStateOf(false) }
    var ignoreNextBlur by remember { mutableStateOf(false) }
    AppSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.search),
        modifier = modifier.onFocusChanged { state ->
            if (hadFocus && !state.isFocused) {
                if (ignoreNextBlur) {
                    ignoreNextBlur = false
                } else {
                    onSearch()
                }
            }
            hadFocus = state.isFocused
        },
        keyboardActions = KeyboardActions(onSearch = {
            ignoreNextBlur = true
            focusManager.clearFocus()
            onSearch()
        })
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductDialog(
    product: Product,
    categories: List<Category>,
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
    onSettingsClick: (Product, Rect) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = 8.dp,
            bottom = 96.dp // Extra bottom padding to avoid FAB overlap
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductItem(
                product = product,
                onSettingsClick = { bounds -> onSettingsClick(product, bounds) }
            )
        }
    }
}

@Composable
private fun ProductItem(
    product: Product,
    onSettingsClick: (Rect) -> Unit
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
                            onSettingsClick(bounds)
                        }
                    }
                },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    buttonPosition = coordinates.localToRoot(Offset.Zero)
                    buttonSize = coordinates.size
                }
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Settings"
                )
            }
        }
    }
}
