package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = Color(0xFF12131A),
    onSecondary = Color(0xFF12131A),
    onTertiary = Color(0xFF12131A),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5D54F6),
    secondary = Color(0xFF00AA72),
    tertiary = Color(0xFFD36224),
    background = Color(0xFFF5F6FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAECEF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B1D26),
    onSurface = Color(0xFF1B1D26),
    onSurfaceVariant = Color(0xFF505A69),
    outline = Color(0xFFD2D5E0)
)

@Composable
fun LocalNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
