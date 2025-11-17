package com.example.listitaapp.ui.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.domain.model.ShoppingList
import com.example.listitaapp.domain.model.User
import com.example.listitaapp.ui.components.AppFab
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.SectionHeader
import com.example.listitaapp.ui.components.OptionsPopupMenu
import com.example.listitaapp.ui.components.PopupMenuAction
import com.example.listitaapp.ui.components.PopupHeaderButton
import com.example.listitaapp.ui.components.PopupHeaderDeleteButton
import com.example.listitaapp.ui.components.AppFormDialog
import com.example.listitaapp.ui.components.AppConfirmDialog
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppSnackbarHost
import com.example.listitaapp.ui.components.StandardCard
import com.example.listitaapp.ui.components.rememberAppSnackbarState
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppTextButton
import com.example.listitaapp.ui.components.appSnackTypeFromMessage
import com.example.listitaapp.ui.components.show
import com.example.listitaapp.ui.components.getGridColumns
import com.example.listitaapp.ui.components.AppSearchField
import com.example.listitaapp.ui.components.AppSearchButton
import com.example.listitaapp.ui.common.asString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    uiState: ShoppingListUiState,
    onCreateList: () -> Unit,
    onListClick: (Long) -> Unit,
    onDeleteList: (Long) -> Unit,
    onUpdateListName: (Long, String) -> Unit,
    onUpdateListDescription: (Long, String) -> Unit,
    onToggleRecurring: (Long, Boolean) -> Unit,
    onShareByEmail: (Long, String) -> Unit,
    onLoadSharedUsers: (Long) -> Unit,
    onRevokeShare: (Long, Long) -> Unit,
    onMakePrivate: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showDeleteDialog by remember { mutableStateOf<ShoppingList?>(null) }
    var selectedList by remember { mutableStateOf<ShoppingList?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showEditListDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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

    if (showRenameDialog && selectedList != null) {
        var newName by remember { mutableStateOf(selectedList!!.name) }
        AppFormDialog(
            title = stringResource(R.string.cambiar_nombre),
            onDismiss = { showRenameDialog = false },
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                onUpdateListName(selectedList!!.id, newName.trim())
                showRenameDialog = false
                showOptions = false
            },
            confirmEnabled = newName.isNotBlank(),
            icon = Icons.Filled.Edit
        ) {
            AppTextField(
                value = newName,
                onValueChange = { if (it.length <= 50) newName = it },
                label = stringResource(R.string.nuevo_nombre)
            )
        }
    }

    if (showEditDescriptionDialog && selectedList != null) {
        var newDescription by remember { mutableStateOf(selectedList!!.description ?: "") }
        AppFormDialog(
            title = stringResource(R.string.cambiar_descripcion),
            onDismiss = { showEditDescriptionDialog = false },
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                onUpdateListDescription(selectedList!!.id, newDescription)
                showEditDescriptionDialog = false
                showOptions = false
            },
            confirmEnabled = true,
            icon = Icons.Filled.Edit
        ) {
            AppTextField(
                value = newDescription,
                onValueChange = { if (it.length <= 200) newDescription = it },
                label = stringResource(R.string.nueva_descripcion),
                singleLine = false,
                minLines = 2,
                maxLines = 4
            )
        }
    }

    if (showEditListDialog && selectedList != null) {
        var newName by remember { mutableStateOf(selectedList!!.name) }
        var newDescription by remember { mutableStateOf(selectedList!!.description ?: "") }
        AppFormDialog(
            title = stringResource(R.string.edit_list),
            onDismiss = { showEditListDialog = false },
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                if (newName != selectedList!!.name) {
                    onUpdateListName(selectedList!!.id, newName.trim())
                }
                if (newDescription != (selectedList!!.description ?: "")) {
                    onUpdateListDescription(selectedList!!.id, newDescription)
                }
                showEditListDialog = false
                showOptions = false
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

    showDeleteDialog?.let { list ->
        AppConfirmDialog(
            message = stringResource(R.string.confirm_delete_list),
            onConfirm = {
                onDeleteList(list.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null },
            destructive = true
        )
    }

    val landscape = com.example.listitaapp.ui.components.isLandscape()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.shopping_lists),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.history)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AppFab(onClick = onCreateList, modifier = Modifier.size(64.dp))
        },
        snackbarHost = {
            if (!landscape) {
                AppSnackbarHost(state = appSnackbar)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val columns = getGridColumns()
            val landscape = com.example.listitaapp.ui.components.isLandscape()
            val horizontalPadding = 16.dp

            if (columns == 1) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontalPadding)
                )
            }

            if (uiState.isLoading && uiState.lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = stringResource(R.string.empty_lists),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                val columns = getGridColumns()
                val extraBottomPadding = if (columns > 1) 96.dp else 0.dp
                val filteredLists = uiState.lists
                val recurrentes = remember(filteredLists) {
                    filteredLists.filter { it.recurring }.sortedBy { it.name.lowercase() }
                }
                val activas = remember(filteredLists) {
                    filteredLists.filter { !it.recurring }.sortedBy { it.name.lowercase() }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = 8.dp,
                        bottom = extraBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (recurrentes.isNotEmpty()) {
                        item {
                            SectionHeader(text = stringResource(R.string.recurrentes))
                        }
                        items(recurrentes, key = { it.id }) { list ->
                            ShoppingListItem(
                                list = list,
                                itemsCount = uiState.itemsCountByListId[list.id],
                                onClick = { onListClick(list.id) },
                                onSettingsClick = { bounds ->
                                    selectedList = list
                                    anchorBounds = bounds
                                    showOptions = true
                                }
                            )
                        }
                    }
                    if (activas.isNotEmpty()) {
                        item {
                            SectionHeader(text = stringResource(R.string.activas))
                        }
                        items(activas, key = { it.id }) { list ->
                            ShoppingListItem(
                                list = list,
                                itemsCount = uiState.itemsCountByListId[list.id],
                                onClick = { onListClick(list.id) },
                                onSettingsClick = { bounds ->
                                    selectedList = list
                                    anchorBounds = bounds
                                    showOptions = true
                                }
                            )
                        }
                    }

                    if (uiState.isLoadingMore) {
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
        }
    }

    val columns = getGridColumns()
    if (columns > 1) {
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
        var localQuery by remember { mutableStateOf(uiState.searchQuery) }

        AppFormDialog(
            title = stringResource(R.string.search),
            onDismiss = { showSearchDialog = false },
            confirmLabel = stringResource(R.string.search),
            onConfirm = {
                onSearchQueryChange(localQuery)
                onSearch()
                showSearchDialog = false
            },
            confirmEnabled = true
        ) {
            AppSearchField(
                value = localQuery,
                onValueChange = { localQuery = it },
                placeholder = stringResource(R.string.search),
                onClear = {
                    localQuery = ""
                    onSearchQueryChange("")
                    onSearch()
                },
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchQueryChange(localQuery)
                        onSearch()
                        showSearchDialog = false
                    }
                )
            )
        }
    }

    if (landscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            AppSnackbarHost(state = appSnackbar)
        }
    }

    OptionsPopupMenu(
        expanded = showOptions && selectedList != null,
        onDismissRequest = { showOptions = false },
        anchorBounds = anchorBounds,
        headerButtons = {
            PopupHeaderButton(
                text = stringResource(R.string.recurrente),
                icon = Icons.Default.History,
                onClick = {
                    selectedList?.let {
                        onToggleRecurring(it.id, it.recurring)
                        showOptions = false
                    }
                }
            )
            PopupHeaderDeleteButton(
                onClick = {
                    selectedList?.let {
                        showDeleteDialog = it
                        showOptions = false
                    }
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
                    selectedList?.let {
                        onMakePrivate(it.id)
                    }
                }
            ),
            PopupMenuAction(
                text = stringResource(R.string.compartir),
                icon = Icons.Default.Share,
                onClick = {
                    selectedList?.let {
                        onLoadSharedUsers(it.id)
                        showShareDialog = true
                    }
                }
            )
        )
    )

    if (showShareDialog && selectedList != null) {
        ShareListDialog(
            sharedUsers = uiState.sharedUsers,
            isLoading = uiState.isLoading,
            onDismiss = {
                showShareDialog = false
                showOptions = false
            },
            onShare = { email ->
                onShareByEmail(selectedList!!.id, email)
            },
            onRevoke = { userId ->
                onRevokeShare(selectedList!!.id, userId)
            },
            onMakePrivate = {
                onMakePrivate(selectedList!!.id)
            }
        )
    }
}

@Composable
private fun ShoppingListItem(
    list: ShoppingList,
    itemsCount: Int?,
    onClick: () -> Unit,
    onSettingsClick: (Rect) -> Unit
) {
    StandardCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (list.description.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val peopleCount = 1 + list.sharedWith.size
                        val peopleText = if (list.sharedWith.isEmpty()) {
                            stringResource(R.string.solo_tu)
                        } else {
                            stringResource(R.string.personas, peopleCount)
                        }
                        Text(
                            text = peopleText + " - " + stringResource(R.string.productos, itemsCount ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = list.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val peopleCount = 1 + list.sharedWith.size
                        val peopleText = if (list.sharedWith.isEmpty()) {
                            stringResource(R.string.solo_tu)
                        } else {
                            stringResource(R.string.personas, peopleCount)
                        }
                        Text(
                            text = peopleText + " - " + stringResource(R.string.productos, itemsCount ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                modifier = Modifier
                    .padding(top = 8.dp)
                    .onGloballyPositioned { coordinates ->
                        buttonPosition = coordinates.localToRoot(Offset.Zero)
                        buttonSize = coordinates.size
                    }
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.configuracion)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?, Boolean) -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var recurring by remember { mutableStateOf(false) }

    AppFormDialog(
        title = stringResource(R.string.create_list),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.create_list),
        confirmEnabled = listName.isNotBlank(),
        onConfirm = {
            if (listName.isNotBlank()) {
                onCreate(listName, description.ifBlank { "" }, recurring)
                onDismiss()
            }
        }
    ) {
        AppTextField(
            value = listName,
            onValueChange = { listName = it },
            label = stringResource(R.string.list_name)
        )

        AppTextField(
            value = description,
            onValueChange = { description = it },
            label = stringResource(R.string.list_description),
            singleLine = false,
            minLines = 2,
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = recurring,
                onCheckedChange = { recurring = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.recurring_list))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareListDialog(
    sharedUsers: List<User>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onRevoke: (Long) -> Unit,
    onMakePrivate: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    
    LaunchedEffect(isLoading) {
        if (!isLoading && email.isNotBlank()) {
            kotlinx.coroutines.delay(300)
            email = ""
        }
    }
    
    AppFormDialog(
        title = stringResource(R.string.compartir),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.compartir),
        confirmEnabled = email.isNotBlank() && !isLoading,
        onConfirm = {
            onShare(email.trim())
        }
    ) {
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            enabled = !isLoading,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.compartido_con),
            style = MaterialTheme.typography.titleMedium
        )

        if (sharedUsers.isEmpty()) {
            Text(
                text = stringResource(R.string.solo_tu),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            sharedUsers.forEachIndexed { index, user ->
                ListItem(
                    headlineContent = { Text(text = "${user.name} ${user.surname}") },
                    supportingContent = { Text(text = user.email) },
                    trailingContent = {
                        AppTextButton(
                            enabled = !isLoading,
                            onClick = { onRevoke(user.id) },
                            text = stringResource(R.string.remove)
                        )
                    }
                )
                if (index < sharedUsers.lastIndex) {
                    Divider()
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        AppTextButton(
            enabled = !isLoading,
            onClick = onMakePrivate,
            text = stringResource(R.string.hacer_privada),
            icon = Icons.Default.Lock,
            contentPadding = PaddingValues(horizontal = 0.dp)
        )
    }
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
