package com.example.listitaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.listitaapp.domain.model.ThemePreference

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = OnBrandPrimary,
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

private val DarkColorScheme = darkColorScheme(
    primary = OnBrandPrimary,
    onPrimary = BrandPrimary,
    primaryContainer = Neutral700,
    onPrimaryContainer = OnBrandPrimary,
    background = Neutral900,
    surface = Neutral800,
    surfaceVariant = Neutral700,
    onSurface = Neutral050,
    onSurfaceVariant = Neutral300,
    secondary = Neutral200,
    tertiary = Neutral300,
    tertiaryContainer = Amber600,
    onTertiaryContainer = Color.Black,
    outline = Neutral500
)

@Composable
fun ListitaAppTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
