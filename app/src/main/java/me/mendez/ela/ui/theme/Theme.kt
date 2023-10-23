package me.mendez.ela.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(11, 57, 84),
    onPrimary = Color.White,

    secondary = Color(191, 215, 234),
    onSecondary = Color.Black,
)

private val LightColorPalette = lightColors(
    primary = Color(11, 57, 84),
    onPrimary = Color.White,

    secondary = Color(191, 215, 234),
    onSecondary = Color.Black,
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
