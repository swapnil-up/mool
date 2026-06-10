package com.mool.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MoolLightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1A6B4C),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    surface = androidx.compose.ui.graphics.Color.White,
    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
)

private val MoolDarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = androidx.compose.ui.graphics.Color(0xFF1A6B4C),
    surface = androidx.compose.ui.graphics.Color(0xFF1C1C1C),
    background = androidx.compose.ui.graphics.Color(0xFF121212),
)

@Composable
fun MoolTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MoolDarkColors else MoolLightColors,
        content = content,
    )
}
