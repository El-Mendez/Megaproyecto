package me.mendez.ela.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.mendez.ela.chat.Message
import me.mendez.ela.ui.general.PopBackTopBar
import me.mendez.ela.ui.screens.chat.components.InputBar
import me.mendez.ela.ui.screens.chat.components.MessageList


@Composable
fun ChatScreen(
    chatName: String,
    messages: List<Message>,
    calculatingResponse: Boolean,
    onSubmit: (String) -> Unit,
    onReturn: (() -> Unit)?,
    showAddToWhitelist: Boolean = false,
    onWhitelistAccept: ((Boolean) -> Unit)? = null,
) {
    val whitelistTemp = fun(value: Boolean) {
        if (onWhitelistAccept != null) {
            onWhitelistAccept(value)
        }
    }

    Scaffold(
        topBar = {
            PopBackTopBar(chatName, onReturn)
        },
        content = {
            MessageList(
                messages = messages,
                writingBubble = calculatingResponse,
                modifier = Modifier.padding(it),
                whitelistVisible = showAddToWhitelist,
                onWhitelistAction = whitelistTemp,
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
