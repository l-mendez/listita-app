package com.example.listitaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = OnBrandPrimary,
    // Used by default FAB
    primaryContainer = Neutral100,
    onPrimaryContainer = BrandPrimary,
    background = SurfaceDefault,
    surface = SurfaceDefault,
    surfaceVariant = SurfaceVariantLight,
    onSurface = BrandPrimary,
    onSurfaceVariant = Neutral600,
    secondary = Neutral700,
    tertiary = Neutral400,
    outline = OutlineNeutral
)

@Composable
fun ListitaAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}