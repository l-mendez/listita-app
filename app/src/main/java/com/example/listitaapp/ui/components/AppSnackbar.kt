package com.example.listitaapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
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
    val landscape = isLandscape()

    SnackbarHost(
        hostState = state.hostState,
        modifier = modifier
    ) { data ->
        val type = state.currentType.value
        val (container, content, actionColor, borderColor) = when (type) {
            AppSnackType.Create -> listOf(
                Color(0xFFE8F5E9),
                Color(0xFF2E7D32),
                Color(0xFF66BB6A),
                Color(0xFFA5D6A7).copy(alpha = 0.5f)
            )
            AppSnackType.Edit -> listOf(
                Color(0xFFF5F5F5),
                Color(0xFF424242),
                Color(0xFF757575),
                Color(0xFFBDBDBD).copy(alpha = 0.5f)
            )
            AppSnackType.Delete -> listOf(
                Color(0xFFFFEBEE),
                Color(0xFFC62828),
                Color(0xFFEF5350),
                Color(0xFFEF9A9A).copy(alpha = 0.5f)
            )
        }
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut()
        ) {
            Snackbar(
                modifier = if (landscape) {
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                        .width(400.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = container,
                contentColor = content,
                action = {
                    val label = data.visuals.actionLabel
                    if (label != null) {
                        AppTextButton(
                            onClick = { data.performAction() },
                            text = label,
                            contentColor = actionColor
                        )
                    }
                }
            ) {
                Text(text = data.visuals.message)
            }
        }
    }
}


