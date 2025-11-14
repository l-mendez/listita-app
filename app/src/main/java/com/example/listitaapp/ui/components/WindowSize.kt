package com.example.listitaapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    Compact,
    Medium,
    Expanded
}

data class WindowSize(
    val width: WindowSizeClass,
    val height: WindowSizeClass
)

@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return WindowSize(
        width = getWindowSizeClass(screenWidth),
        height = getWindowSizeClass(screenHeight)
    )
}

private fun getWindowSizeClass(size: Dp): WindowSizeClass {
    return when {
        size < 600.dp -> WindowSizeClass.Compact
        size < 840.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

@Composable
fun getGridColumns(): Int {
    val windowSize = rememberWindowSize()
    val landscape = isLandscape()

    return when {
        windowSize.width == WindowSizeClass.Expanded -> if (landscape) 4 else 3
        windowSize.width == WindowSizeClass.Medium -> if (landscape) 3 else 2
        landscape -> 2
        else -> 1
    }
}
