package me.mendez.ela.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import me.mendez.ela.R
import me.mendez.ela.model.MessageData

@Composable
fun MessageList(messages: List<MessageData>, writingBubble: Boolean, modifier: Modifier) {
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

    Box(modifier.background(brush)) {
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

