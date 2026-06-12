package com.mool.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MoolGreen = Color(0xFF1A6B4C)
val MoolGreenLight = Color(0xFF4CAF50)
val MoolGreenDark = Color(0xFF0D4A34)
val MoolGreenBright = Color(0xFF66BB6A)
val MoolSurfaceLight = Color(0xFFF8F9FA)
val MoolSurfaceDark = Color(0xFF1A1A2E)
val MoolBackgroundLight = Color(0xFFF0F2F5)
val MoolBackgroundDark = Color(0xFF0F0F1A)
val MoolError = Color(0xFFD32F2F)
val MoolErrorLight = Color(0xFFFFCDD2)
val MoolOnErrorLight = Color(0xFF5F2120)
val MoolGold = Color(0xFFFFB300)

private val MoolLightColors = lightColorScheme(
    primary = MoolGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0D4A34),
    secondary = MoolGreenLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFF1976D2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF0D47A1),
    error = MoolError,
    onError = Color.White,
    errorContainer = MoolErrorLight,
    onErrorContainer = MoolOnErrorLight,
    surface = MoolSurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8E8ED),
    onSurfaceVariant = Color(0xFF49454F),
    background = MoolBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFD0D0D5),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = MoolGreenBright,
    surfaceTint = MoolGreen,
)

private val MoolDarkColors = darkColorScheme(
    primary = MoolGreenBright,
    onPrimary = Color(0xFF00391F),
    primaryContainer = MoolGreenDark,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = MoolGreenLight,
    onSecondary = Color(0xFF00391F),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF64B5F6),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF0D47A1),
    onTertiaryContainer = Color(0xFFBBDEFB),
    error = Color(0xFFEF5350),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF601410),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = MoolSurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF262633),
    onSurfaceVariant = Color(0xFFCAC4D0),
    background = MoolBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    outline = Color(0xFF6F6F7A),
    outlineVariant = Color(0xFF46464F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = MoolGreen,
    surfaceTint = MoolGreenBright,
)

private val MoolTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
)

private val MoolShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

data class MoolElevations(
    val none: Dp = 0.dp,
    val card: Dp = 1.dp,
    val cardHovered: Dp = 4.dp,
    val raised: Dp = 6.dp,
    val navBar: Dp = 8.dp,
)

val LocalMoolElevations = compositionLocalOf { MoolElevations() }

enum class ThemeMode { FOLLOW_SYSTEM, LIGHT, DARK }

@Composable
fun MoolTheme(
    themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) MoolDarkColors else MoolLightColors,
        typography = MoolTypography,
        shapes = MoolShapes,
        content = content,
    )
}
