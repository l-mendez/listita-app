package com.example.listitaapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = OnBrandPrimary,
    primaryContainer = Neutral300,
    onPrimaryContainer = BrandPrimary,
    background = Color(0xFF0B0B0B),
    surface = Color(0xFF0F0F0F),
    surfaceVariant = Neutral800,
    onSurface = Color.White,
    onSurfaceVariant = Neutral300,
    secondary = Neutral600,
    tertiary = Neutral400,
    outline = Neutral600
)

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep colors consistent with our design system
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}