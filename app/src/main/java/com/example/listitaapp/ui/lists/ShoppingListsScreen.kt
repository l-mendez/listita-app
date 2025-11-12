package com.example.listitaapp.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<ShoppingList?>(null) }
    var selectedList by remember { mutableStateOf<ShoppingList?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }

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

    // Rename dialog
    if (showRenameDialog && selectedList != null) {
        var newName by remember { mutableStateOf(selectedList!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.cambiar_nombre)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 50) newName = it },
                    label = { Text(stringResource(R.string.nuevo_nombre)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateListName(selectedList!!.id, newName.trim())
                        showRenameDialog = false
                        showOptions = false
                    },
                    enabled = newName.isNotBlank()
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
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
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { if (it.length <= 200) newDescription = it },
                    label = { Text(stringResource(R.string.nueva_descripcion)) },
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateListDescription(selectedList!!.id, newDescription)
                        showEditDescriptionDialog = false
                        showOptions = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDescriptionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { list ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.confirm)) },
            text = { Text(stringResource(R.string.confirm_delete_list)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteList(list.id)
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
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_lists)) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateList) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recurrentes.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.recurrentes),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
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
                            Text(
                                text = stringResource(R.string.activas),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
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
        ModalBottomSheet(onDismissRequest = { showOptions = false }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = selectedList!!.recurring,
                    onClick = {
                        onToggleRecurring(selectedList!!.id, selectedList!!.recurring)
                        showOptions = false
                    },
                    label = { Text(stringResource(R.string.recurrente)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) }
                )
                TextButton(onClick = {
                    showOptions = false
                    showDeleteDialog = selectedList
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.cambiar_nombre)) },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable { showRenameDialog = true }
            )
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.cambiar_descripcion)) },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable { showEditDescriptionDialog = true }
            )
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.hacer_privada)) },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingContent = { Text("(próximamente)", color = MaterialTheme.colorScheme.outline) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.compartir)) },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                trailingContent = { Text("(próximamente)", color = MaterialTheme.colorScheme.outline) }
            )
            Spacer(Modifier.height(16.dp))
        }
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (list.recurring) Icons.Default.Refresh else Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (list.description?.isNotBlank() == true) {
                    Text(
                        text = list.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
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

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_list),
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text(stringResource(R.string.list_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.list_description)) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
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
                            if (listName.isNotBlank()) {
                                onCreate(
                                    listName,
                                    description.ifBlank { "" },
                                    recurring
                                )
                                onDismiss()
                            }
                        },
                        enabled = listName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.create_list))
                    }
                }
            }
        }
    }
}
