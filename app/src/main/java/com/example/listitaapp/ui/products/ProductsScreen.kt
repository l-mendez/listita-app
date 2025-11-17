package com.example.listitaapp.ui.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.listitaapp.R
import com.example.listitaapp.domain.model.Product
import com.example.listitaapp.domain.model.Category
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.OptionsPopupMenu
import com.example.listitaapp.ui.components.PopupMenuAction
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
import com.example.listitaapp.ui.components.CreateCategoryDialog
import com.example.listitaapp.ui.components.CreateProductDialog
import com.example.listitaapp.ui.components.rememberWindowSize
import com.example.listitaapp.ui.components.WindowSizeClass
import com.example.listitaapp.ui.components.isLandscape
import com.example.listitaapp.ui.common.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    uiState: ProductUiState,
    categories: List<Category>,
    onCreateProduct: () -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onUpdateProduct: (Long, String?, Long?) -> Unit,
    onCreateCategory: (String, (Category) -> Unit) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
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
    val windowSize = rememberWindowSize()
    val landscape = isLandscape()
    val showSearchButton = windowSize.width != WindowSizeClass.Compact || landscape
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.hasNextPage, uiState.isLoadingMore, uiState.isLoading) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }.distinctUntilChanged().collect { shouldLoadMore ->
            if (shouldLoadMore && uiState.hasNextPage && !uiState.isLoadingMore && !uiState.isLoading) {
                onLoadMore()
            }
        }
    }

    uiState.error?.let {
        AppMessageDialog(
            type = AppDialogType.Error,
            message = it,
            onDismiss = onClearError
        )
    }

    uiState.successMessage?.let { message ->
        val localizedMessage = message.asString()
        LaunchedEffect(message) {
            if (localizedMessage.isNotBlank()) {
                appSnackbar.show(localizedMessage, appSnackTypeFromMessage(localizedMessage))
            }
            onClearSuccess()
        }
    }

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

    editingProduct?.let { product ->
        EditProductDialog(
            product = product,
            categories = categories,
            onDismiss = { editingProduct = null },
            onApply = { name, categoryId ->
                onUpdateProduct(product.id, name, categoryId)
            },
            onCreateCategory = onCreateCategory
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
            val horizontalPadding = 16.dp

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

            if (uiState.isLoading && uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.products.isEmpty()) {
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
                ProductsList(
                    products = uiState.products,
                    listState = listState,
                    showLoadingMore = uiState.isLoadingMore,
                    onSettingsClick = { product, bounds ->
                        selectedProduct = product
                        anchorBounds = bounds
                        showOptions = true
                    },
                    horizontalPadding = horizontalPadding,
                    extraBottomPadding = if (showSearchButton) 96.dp else 0.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showSearchButton) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 124.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AppSearchButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier.size(64.dp)
            )
        }
    }

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
                placeholder = stringResource(R.string.search),
                onClear = onSearch
            )
        }
    }

    OptionsPopupMenu(
        expanded = showOptions && selectedProduct != null,
        onDismissRequest = { showOptions = false },
        anchorBounds = anchorBounds,
        headerButtons = null,
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
        onClear = {
            ignoreNextBlur = true
            focusManager.clearFocus()
            onSearch()
        },
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

@Composable
private fun EditProductDialog(
    product: Product,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (name: String?, categoryId: Long?) -> Unit,
    onCreateCategory: (String, (Category) -> Unit) -> Unit
) {
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var productName by rememberSaveable(product.id) { mutableStateOf(product.name) }
    var selectedCategoryId by rememberSaveable(product.id) { mutableStateOf(product.category?.id) }
    var pendingCategoryId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(categories) {
        if (
            selectedCategoryId != null &&
            categories.isNotEmpty() &&
            categories.none { it.id == selectedCategoryId }
        ) {
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
                productName = product.name
                selectedCategoryId = product.category?.id
                showCreateCategoryDialog = false
                pendingCategoryId = null
                onDismiss()
            },
            onConfirm = { name, categoryId ->
                onApply(name.ifBlank { null }, categoryId)
            },
            onRequestCreateCategory = {
                pendingCategoryId = null
                showCreateCategoryDialog = true
            },
            titleResId = R.string.edit,
            confirmLabelResId = R.string.save
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

@Composable
private fun ProductsList(
    products: List<Product>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    showLoadingMore: Boolean,
    onSettingsClick: (Product, Rect) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    extraBottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = 8.dp,
            bottom = extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductItem(
                product = product,
                onSettingsClick = { bounds -> onSettingsClick(product, bounds) }
            )
        }

        if (showLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
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
