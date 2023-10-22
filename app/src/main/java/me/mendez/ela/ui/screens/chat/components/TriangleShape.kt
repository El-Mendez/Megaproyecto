package me.mendez.ela.ui.screens.chat.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// fuente: https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class TriangleEdgeShape(private val offset: Int, private val start: Boolean) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = if (start) {
            Path().apply {
                moveTo(x = 0f, y = size.height - offset)
                lineTo(x = 0f, y = size.height)
                lineTo(x = 0f + offset, y = size.height)
            }
        } else {
            Path().apply {
                moveTo(x = size.width, y = size.height - offset)
                lineTo(x = size.width, y = size.height)
                lineTo(x = size.width - offset, y = size.height)
            }
        }
        return Outline.Generic(path = trianglePath)
    }
}
