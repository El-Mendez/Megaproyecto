package me.mendez.ela.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(84, 184, 255),
    onPrimary = Color(72, 60, 12),

    secondary = Color(255, 235, 0),
    onSecondary = Color(72, 62, 139),
)

private val LightColorPalette = lightColors(
    primary = Color(84, 184, 255),
    onPrimary = Color(72, 60, 12),

    secondary = Color(255, 235, 0),
    onSecondary = Color(72, 62, 139),
)

@Composable
fun ElaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}