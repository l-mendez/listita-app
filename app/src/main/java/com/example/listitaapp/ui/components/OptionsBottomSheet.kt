package com.example.listitaapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.composed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.listitaapp.R

/**
 * Reusable options bottom sheet to keep a consistent style across the app.
 * Consumers provide optional header and the body content (list of actions, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    onDismissRequest: () -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        headerContent?.invoke()
        content()
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Standardized action row used inside [OptionsBottomSheet].
 * Adds a divider before each item for visual separation.
 */
@Composable
fun ColumnScope.SheetActionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Divider()
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailing,
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple(onClick)
    )
}

/**
 * Convenience header with a trailing destructive delete action.
 */
@Composable
fun SheetHeaderWithDelete(
    onDeleteClick: () -> Unit,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        leadingContent?.invoke()
        TextButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
        }
    }
}

// Lightweight no-ripple clickable used to match default ListItem behavior with our own Divider usage.
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier = composed {
    val clickAction = remember { onClick }
    this.then(
        clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { clickAction() }
    )
}

