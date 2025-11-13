package com.example.listitaapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AppSnackType { Create, Edit, Delete }

class AppSnackbarState(
    val hostState: SnackbarHostState,
    internal val currentType: MutableState<AppSnackType>
)

@Composable
fun rememberAppSnackbarState(
    initialType: AppSnackType = AppSnackType.Edit
): AppSnackbarState {
    val host = remember { SnackbarHostState() }
    val type = remember { mutableStateOf(initialType) }
    return AppSnackbarState(hostState = host, currentType = type)
}

suspend fun AppSnackbarState.show(
    message: String,
    type: AppSnackType,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    currentType.value = type
    hostState.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = actionLabel == null,
        duration = duration
    )
}

fun appSnackTypeFromMessage(message: String): AppSnackType {
    val m = message.lowercase()
    return when {
        listOf("elimin", "delete", "removed", "removido", "borr").any { m.contains(it) } -> AppSnackType.Delete
        listOf("cread", "created", "agreg", "added").any { m.contains(it) } -> AppSnackType.Create
        else -> AppSnackType.Edit
    }
}

@Composable
fun AppSnackbarHost(
    state: AppSnackbarState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = state.hostState,
        modifier = modifier
    ) { data ->
        val type = state.currentType.value
        val (container, content, actionColor) = when (type) {
            AppSnackType.Create -> Triple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.primary
            )
            AppSnackType.Edit -> Triple(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                MaterialTheme.colorScheme.secondary
            )
            AppSnackType.Delete -> Triple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                MaterialTheme.colorScheme.error
            )
        }
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut()
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                containerColor = container,
                contentColor = content,
                action = {
                    val label = data.visuals.actionLabel
                    if (label != null) {
                        TextButton(
                            onClick = { data.performAction() }
                        ) {
                            Text(label, color = actionColor)
                        }
                    }
                }
            ) {
                Text(text = data.visuals.message)
            }
        }
    }
}


