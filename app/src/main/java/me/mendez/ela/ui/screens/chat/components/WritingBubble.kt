package me.mendez.ela.ui.screens.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Preview
@Composable
fun WritingBubble() {
    val offset1 = remember { Animatable(0f) }
    val offset2 = remember { Animatable(0f) }
    val offset3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offset1.animateTo(
            targetValue = -5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, 500),
                repeatMode = RepeatMode.Reverse,
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(200)
        offset2.animateTo(
            targetValue = -5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, 500),
                repeatMode = RepeatMode.Reverse,
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(400)
        offset3.animateTo(
            targetValue = -5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, 500),
                repeatMode = RepeatMode.Reverse,
            )
        )
    }

    Surface(
        color = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(8.dp),
        elevation = 5.dp,
        modifier = Modifier
            .height(40.dp)
            .padding(vertical = 5.dp, horizontal = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .offset(y = 5.dp)
        ) {
            Text(
                text = "⬤",
                style = MaterialTheme.typography.body1.plus(TextStyle(color = MaterialTheme.colors.onPrimary)),
                modifier = Modifier.offset(y = offset1.value.dp)
            )
            Text(
                text = "⬤",
                style = MaterialTheme.typography.body1.plus(TextStyle(color = MaterialTheme.colors.onPrimary)),
                modifier = Modifier.offset(y = offset2.value.dp)
            )
            Text(
                text = "⬤",
                style = MaterialTheme.typography.body1.plus(TextStyle(color = MaterialTheme.colors.onPrimary)),
                modifier = Modifier.offset(y = offset3.value.dp)
            )
        }
    }
}
