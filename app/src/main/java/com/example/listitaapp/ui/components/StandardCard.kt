package com.example.listitaapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val borderModifier = Modifier.border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        shape = shape
    )
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.then(borderModifier),
            shape = shape,
            colors = colors,
            elevation = elevation
        ) { content() }
    } else {
        Card(
            modifier = modifier.then(borderModifier),
            shape = shape,
            colors = colors,
            elevation = elevation
        ) { content() }
    }
}


