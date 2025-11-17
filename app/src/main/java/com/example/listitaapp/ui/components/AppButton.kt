package com.example.listitaapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    icon: ImageVector? = null
) {
    val buttonModifier = if (fullWidth) {
        modifier.fillMaxWidth().height(AppComponentDefaults.ButtonHeight)
    } else {
        modifier
    }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppComponentDefaults.UnifiedCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppComponentDefaults.ButtonEnabledColor,
            contentColor = Color.White,
            disabledContainerColor = AppComponentDefaults.ButtonDisabledColor,
            disabledContentColor = AppComponentDefaults.ButtonDisabledContentColor
        ),
        modifier = buttonModifier
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(text)
        }
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(AppComponentDefaults.UnifiedCornerRadius),
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor ?: MaterialTheme.colorScheme.primary,
            disabledContentColor = AppComponentDefaults.ButtonDisabledColor
        ),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun AppDestructiveButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    icon: ImageVector? = null
) {
    val buttonModifier = if (fullWidth) {
        modifier.fillMaxWidth().height(AppComponentDefaults.ButtonHeight)
    } else {
        modifier
    }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppComponentDefaults.UnifiedCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = AppComponentDefaults.ButtonDisabledColor,
            disabledContentColor = AppComponentDefaults.ButtonDisabledContentColor
        ),
        modifier = buttonModifier
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp
            )
        } else {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(text)
        }
    }
}
