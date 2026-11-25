package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.model.MapColorPalette
import com.example.model.MapVisualThemeMode
import com.example.model.paletteForMode

val LocalMapPalette = compositionLocalOf { paletteForMode(MapVisualThemeMode.NIGHT) }

fun contrastContentColor(background: Color): Color =
    if (background.luminance() > 0.36f) Color(0xFF020617) else Color.White

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Slate950,
    primaryContainer = Slate850,
    onPrimaryContainer = CyanNeon,
    secondary = AmberRadar,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = AmberRadar,
    tertiary = EmeraldVerified,
    onTertiary = Slate950,
    tertiaryContainer = Slate800,
    onTertiaryContainer = EmeraldVerified,
    background = Slate950,
    onBackground = Slate100,
    surface = DarkSurface,
    onSurface = Slate100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate300,
    outline = DarkBorder,
    error = CrimsonThreat,
    onError = Slate950
)

private val HighContrastColorScheme = darkColorScheme(
    primary = Color(0xFF40C8FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0A0F1A),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFD60A),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFFFD60A),
    tertiary = Color(0xFF30D158),
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color.White,
    outline = Color.White,
    error = Color(0xFFFF3B30),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF00363D),
    secondary = AmberDim,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Slate700,
    outline = Color(0xFF64748B),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun VeriScopeTheme(
    visualThemeMode: MapVisualThemeMode = MapVisualThemeMode.NIGHT,
    darkTheme: Boolean = visualThemeMode != MapVisualThemeMode.DAY,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val mapPalette = remember(visualThemeMode) { paletteForMode(visualThemeMode) }
    val colorScheme = when {
        visualThemeMode == MapVisualThemeMode.HIGH_CONTRAST -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalMapPalette provides mapPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
