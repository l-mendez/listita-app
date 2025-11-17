package com.example.listitaapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.listitaapp.R

enum class AppDialogType {
    Error, Warning, Success, Info
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppMessageDialog(
    type: AppDialogType,
    message: String,
    onDismiss: () -> Unit,
    title: String? = null,
    confirmLabel: String = stringResource(id = R.string.ok),
    icon: ImageVector? = null
) {
    val (defaultTitle, defaultIcon, iconTint, iconBg) = when (type) {
        AppDialogType.Error -> Quadruple(
            stringResource(id = R.string.error),
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.errorContainer
        )
        AppDialogType.Warning -> Quadruple(
            stringResource(id = R.string.confirm),
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        AppDialogType.Success -> Quadruple(
            stringResource(id = R.string.success),
            Icons.Filled.CheckCircle,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
        AppDialogType.Info -> Quadruple(
            stringResource(id = R.string.info),
            Icons.Filled.Info,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(56.dp)
                            .background(iconBg, CircleShape),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon ?: defaultIcon,
                            contentDescription = null,
                            tint = iconTint
                        )
                    }

                    Text(
                        text = title ?: defaultTitle,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppTextButton(
                            onClick = onDismiss,
                            text = confirmLabel
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(id = R.string.confirm),
    confirmLabel: String = stringResource(id = R.string.delete),
    dismissLabel: String = stringResource(id = R.string.cancel),
    destructive: Boolean = true,
    icon: ImageVector = Icons.Filled.Delete
) {
    val confirmContainer: Color
    val confirmContent: Color
    val iconBg: Color
    val iconTint: Color
    if (destructive) {
        confirmContainer = MaterialTheme.colorScheme.error
        confirmContent = MaterialTheme.colorScheme.onError
        iconBg = MaterialTheme.colorScheme.errorContainer
        iconTint = MaterialTheme.colorScheme.onErrorContainer
    } else {
        confirmContainer = MaterialTheme.colorScheme.primary
        confirmContent = MaterialTheme.colorScheme.onPrimary
        iconBg = MaterialTheme.colorScheme.primaryContainer
        iconTint = MaterialTheme.colorScheme.onPrimaryContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(56.dp)
                            .background(iconBg, CircleShape),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint)
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppTextButton(
                            onClick = onDismiss,
                            text = dismissLabel
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        if (destructive) {
                            AppDestructiveButton(
                                onClick = onConfirm,
                                text = confirmLabel
                            )
                        } else {
                            AppButton(
                                onClick = onConfirm,
                                text = confirmLabel
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppFormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    dismissLabel: String = stringResource(id = R.string.cancel),
    icon: ImageVector? = null,
    iconBg: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(iconBg, CircleShape),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = iconTint)
                        }
                    }

                    Text(text = title, style = MaterialTheme.typography.titleLarge)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        content = content
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppTextButton(
                            onClick = onDismiss,
                            text = dismissLabel
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AppButton(
                            onClick = onConfirm,
                            text = confirmLabel,
                            enabled = confirmEnabled
                        )
                    }
                }
            }
        }
    }
}

