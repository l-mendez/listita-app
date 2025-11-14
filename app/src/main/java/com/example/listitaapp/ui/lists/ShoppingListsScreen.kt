package com.example.listitaapp.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R
import com.example.listitaapp.data.model.ShoppingList
import com.example.listitaapp.ui.components.AppFab
import com.example.listitaapp.ui.components.AppTopBar
import com.example.listitaapp.ui.components.SectionHeader
import com.example.listitaapp.ui.components.OptionsBottomSheet
import com.example.listitaapp.ui.components.SheetActionItem
import com.example.listitaapp.ui.components.SheetHeaderWithDelete
import com.example.listitaapp.ui.components.AppFormDialog
import com.example.listitaapp.ui.components.AppConfirmDialog
import com.example.listitaapp.ui.components.AppDialogType
import com.example.listitaapp.ui.components.AppMessageDialog
import com.example.listitaapp.ui.components.AppSnackbarHost
import com.example.listitaapp.ui.components.rememberAppSnackbarState
import com.example.listitaapp.ui.components.AppTextField
import com.example.listitaapp.ui.components.AppTextButton
import com.example.listitaapp.ui.components.appSnackTypeFromMessage
import com.example.listitaapp.ui.components.show
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Shopping Lists Screen - Following HCI Principles:
 * - Visibility of system status
 * - User control and freedom
 * - Recognition over recall
 * - Flexibility and efficiency
 * - Aesthetic and minimalist design
 */
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
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val appSnackbar = rememberAppSnackbarState()
    var showDeleteDialog by remember { mutableStateOf<ShoppingList?>(null) }
    var selectedList by remember { mutableStateOf<ShoppingList?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showEditListDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

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

    // Rename dialog
    if (showRenameDialog && selectedList != null) {
        var newName by remember { mutableStateOf(selectedList!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.cambiar_nombre)) },
            text = {
                AppTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 50) newName = it },
                    label = stringResource(R.string.nuevo_nombre)
                )
            },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        onUpdateListName(selectedList!!.id, newName.trim())
                        showRenameDialog = false
                        showOptions = false
                    },
                    text = stringResource(R.string.save),
                    enabled = newName.isNotBlank()
                )
            },
            dismissButton = {
                AppTextButton(
                    onClick = { showRenameDialog = false },
                    text = stringResource(R.string.cancel)
                )
            }
        )
    }

    // Edit description dialog
    if (showEditDescriptionDialog && selectedList != null) {
        var newDescription by remember { mutableStateOf(selectedList!!.description ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDescriptionDialog = false },
            title = { Text(stringResource(R.string.cambiar_descripcion)) },
            text = {
                AppTextField(
                    value = newDescription,
                    onValueChange = { if (it.length <= 200) newDescription = it },
                    label = stringResource(R.string.nueva_descripcion),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        onUpdateListDescription(selectedList!!.id, newDescription)
                        showEditDescriptionDialog = false
                        showOptions = false
                    },
                    text = stringResource(R.string.save)
                )
            },
            dismissButton = {
                AppTextButton(
                    onClick = { showEditDescriptionDialog = false },
                    text = stringResource(R.string.cancel)
                )
            }
        )
    }

    // Combined Edit dialog (name + description)
    if (showEditListDialog && selectedList != null) {
        var newName by remember { mutableStateOf(selectedList!!.name) }
        var newDescription by remember { mutableStateOf(selectedList!!.description ?: "") }
        AlertDialog(
            onDismissRequest = { showEditListDialog = false },
            title = { Text(stringResource(R.string.edit_list)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        if (newName != selectedList!!.name) {
                            onUpdateListName(selectedList!!.id, newName.trim())
                        }
                        if (newDescription != (selectedList!!.description ?: "")) {
                            onUpdateListDescription(selectedList!!.id, newDescription)
                        }
                        showEditListDialog = false
                        showOptions = false
                    },
                    text = stringResource(R.string.save),
                    enabled = newName.isNotBlank()
                )
            },
            dismissButton = {
                AppTextButton(
                    onClick = { showEditListDialog = false },
                    text = stringResource(R.string.cancel)
                )
            }
        )
    }

    // Delete confirmation dialog (standardized)
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.shopping_lists),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = stringResource(R.string.history)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AppFab(onClick = onCreateList, modifier = Modifier.size(64.dp))
        },
        snackbarHost = { AppSnackbarHost(state = appSnackbar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Loading indicator
            if (uiState.isLoading && uiState.lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.lists.isEmpty()) {
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
                            imageVector = Icons.Default.List,
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
                // Lists display grouped
                val recurrentes = remember(uiState.lists) {
                    uiState.lists.filter { it.recurring }.sortedBy { it.name.lowercase() }
                }
                val activas = remember(uiState.lists) {
                    uiState.lists.filter { !it.recurring }.sortedBy { it.name.lowercase() }
                }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                onSettingsClick = {
                                    selectedList = list
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
                                onSettingsClick = {
                                    selectedList = list
                                    showOptions = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Options bottom sheet
    if (showOptions && selectedList != null) {
        OptionsBottomSheet(onDismissRequest = { showOptions = false },
            headerContent = {
                SheetHeaderWithDelete(
                    onDeleteClick = {
                        showOptions = false
                        showDeleteDialog = selectedList
                    },
                    leadingContent = {
                        FilterChip(
                            selected = selectedList!!.recurring,
                            onClick = {
                                onToggleRecurring(selectedList!!.id, selectedList!!.recurring)
                                showOptions = false
                            },
                            label = { Text(stringResource(R.string.recurrente)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) }
                        )
                    }
                )
            }
        ) {
            SheetActionItem(
                text = stringResource(R.string.edit),
                icon = Icons.Default.Edit,
                onClick = { showEditListDialog = true }
            )
            SheetActionItem(
                text = stringResource(R.string.hacer_privada),
                icon = Icons.Default.Lock,
                onClick = {
                    onMakePrivate(selectedList!!.id)
                    showOptions = false
                }
            )
            SheetActionItem(
                text = stringResource(R.string.compartir),
                icon = Icons.Default.Share,
                onClick = {
                    onLoadSharedUsers(selectedList!!.id)
                    showShareDialog = true
                }
            )
        }
    }

    // Share dialog
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
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleLarge
                )
                if (!list.description.isNullOrBlank()) {
                    Text(
                        text = list.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

            IconButton(onClick = onSettingsClick) {
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
    sharedUsers: List<com.example.listitaapp.data.model.User>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onRevoke: (Long) -> Unit,
    onMakePrivate: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    
    // Clear email when loading completes successfully
    LaunchedEffect(isLoading) {
        if (!isLoading && email.isNotBlank()) {
            // Small delay to allow user to see success feedback
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
            sharedUsers.forEach { user ->
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
                Divider()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        AppTextButton(
            enabled = !isLoading,
            onClick = onMakePrivate,
            text = stringResource(R.string.hacer_privada),
            icon = Icons.Default.Lock
        )
    }
}
