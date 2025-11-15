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
    background = BackgroundDefault,
    surface = SurfaceDefault,
    surfaceVariant = SurfaceVariantLight,
    onSurface = BrandPrimary,
    onSurfaceVariant = Neutral600,
    secondary = Neutral700,
    tertiary = Neutral400,
    tertiaryContainer = Amber100, // Subtle yellow/amber for recurring chip
    onTertiaryContainer = Amber900, // Dark amber text for contrast
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