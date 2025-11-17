package com.example.listitaapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AppFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AppComponentDefaults.ButtonEnabledColor,
        contentColor = Color.White,
        shape = RoundedCornerShape(AppComponentDefaults.UnifiedCornerRadius),
        modifier = modifier
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}


