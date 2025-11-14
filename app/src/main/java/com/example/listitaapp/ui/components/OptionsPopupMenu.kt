package com.example.listitaapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.listitaapp.R

/**
 * Data class representing a menu action item.
 */
data class PopupMenuAction(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val destructive: Boolean = false // If true, displays in red color
)

/**
 * Reusable options popup menu component that displays a popup menu near an anchor composable.
 * This replaces the bottom sheet approach with a more contextual popup menu.
 * 
 * Usage:
 * ```
 * var showMenu by remember { mutableStateOf(false) }
 * var anchorBounds by remember { mutableStateOf<Rect?>(null) }
 * 
 * IconButton(
 *     onClick = { showMenu = true },
 *     modifier = Modifier.onGloballyPositioned { coordinates ->
 *         anchorBounds = coordinates.boundsInRoot
 *     }
 * ) {
 *     Icon(Icons.Default.MoreVert, contentDescription = "Settings")
 * }
 * 
 * OptionsPopupMenu(
 *     expanded = showMenu,
 *     onDismissRequest = { showMenu = false },
 *     anchorBounds = anchorBounds,
 *     headerButtons = { ... },
 *     actions = listOf(...)
 * )
 * ```
 * 
 * @param expanded Whether the popup is currently visible
 * @param onDismissRequest Callback when the popup should be dismissed
 * @param anchorBounds The bounds of the anchor composable (obtained via onGloballyPositioned)
 * @param headerButtons Optional top row buttons (e.g., Recurring toggle and Delete)
 * @param actions List of menu actions to display below the header
 */
@Composable
fun OptionsPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect?,
    modifier: Modifier = Modifier,
    headerButtons: (@Composable RowScope.() -> Unit)? = null,
    actions: List<PopupMenuAction> = emptyList()
) {
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val menuWidth = 200.dp

    if (expanded && anchorBounds != null) {
        val offset = with(density) {
            val menuWidthPx = menuWidth.toPx()
            val spacing = 8.dp.toPx()

            val xPosition = if (anchorBounds.left - menuWidthPx - spacing < 0) {
                (anchorBounds.right + spacing).toInt()
            } else {
                (anchorBounds.left - menuWidthPx - spacing).toInt()
            }

            IntOffset(
                x = xPosition,
                y = (anchorBounds.top - 8.dp.toPx()).toInt()
            )
        }

        Popup(
            onDismissRequest = onDismissRequest,
            alignment = Alignment.TopStart,
            offset = offset,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = modifier.width(menuWidth),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Header buttons row (e.g., Recurring toggle and Delete)
                    headerButtons?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            it()
                        }
                        if (actions.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )
                        }
                    }

                    // Menu actions
                    actions.forEachIndexed { index, action ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                thickness = 1.dp
                            )
                        }
                        PopupMenuItem(
                            text = action.text,
                            icon = action.icon,
                            onClick = {
                                action.onClick()
                                onDismissRequest()
                            },
                            destructive = action.destructive
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual menu item within the popup menu.
 */
@Composable
private fun PopupMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false
) {
    val iconColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableWithoutRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

/**
 * Convenience composable for a header button with icon and label (e.g., Recurring toggle).
 */
@Composable
fun RowScope.PopupHeaderButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .clickableWithoutRipple(onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) {
                Color(0xFFFFD700) // Gold/Yellow color for selected star
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                Color(0xFFFFD700) // Gold/Yellow color for selected text
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Convenience composable for a destructive header button (e.g., Delete).
 */
@Composable
fun RowScope.PopupHeaderDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .clickableWithoutRipple(onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(R.string.delete),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// Lightweight no-ripple clickable used to match default behavior.
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier = 
    composed {
        val clickAction = remember { onClick }
        this.then(
            clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { clickAction() }
        )
    }

