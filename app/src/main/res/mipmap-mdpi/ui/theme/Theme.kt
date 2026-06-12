package com.example.ui.theme

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
    primary = CobaltPrimary,
    secondary = NeonCyanAccent,
    tertiary = RoyalViolet,
    background = SpaceDarkBackground,
    surface = GlassSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = SlateSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default
    dynamicColor: Boolean = false, // Keep consistent professional branding colors
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme // Utilize our bespoke custom color scheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

