package me.mendez.ela.ui.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import me.mendez.ela.R
import me.mendez.ela.chat.Message

@Composable
fun MessageList(
    messages: List<Message>,
    writingBubble: Boolean,
    modifier: Modifier,
    whitelistVisible: Boolean,
    onWhitelistAction: ((Boolean) -> Unit)
) {
    val image = ImageBitmap.imageResource(R.drawable.background_tile)
    val lazyListState = rememberLazyListState()
    val brush = remember {
        ShaderBrush(
            ImageShader(
                image,
                TileMode.Repeated,
                TileMode.Repeated,
            )
        )
    }

    LaunchedEffect(messages, writingBubble) {
        lazyListState.scrollToItem(maxOf(messages.size - 1, 0))
    }

    Column(modifier.background(brush)) {
        AnimatedVisibility(whitelistVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.9f))
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "¿Siempre permitir el tráfico de esta página?",
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onWhitelistAction(true) },
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text("Sí")
                }

                IconButton(
                    onClick = { onWhitelistAction(false) },
                ) {
                    Icon(
                        Icons.Filled.Close,
                        "cancelar",
                    )
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(messages) { message ->
                MessageBubble(content = message)
            }
            if (writingBubble) {
                item {
                    WritingBubble()
                }
            }
        }
    }
}
