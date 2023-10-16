package me.mendez.ela.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.mendez.ela.model.MessageData
import me.mendez.ela.ui.general.PopBackTopBar
import me.mendez.ela.ui.screens.chat.components.InputBar
import me.mendez.ela.ui.screens.chat.components.MessageList


@Composable
fun ChatScreen(
    messages: List<MessageData>,
    calculatingResponse: Boolean,
    onSubmit: (String) -> Unit,
    onReturn: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            PopBackTopBar("Ela", onReturn)
        },
        content = {
            MessageList(
                messages = messages,
                writingBubble = calculatingResponse,
                modifier = Modifier.padding(it),
            )
        },
        bottomBar = {
            InputBar(
                onSubmit = onSubmit,
                submitEnabled = !calculatingResponse,
            )
        }
    )
}
