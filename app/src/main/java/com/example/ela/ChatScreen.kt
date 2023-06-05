package com.example.ela

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class MessageData(
    val content: String,
    val userCreated: Boolean,
)

@Composable
@Preview
fun ChatScreen() {
    val messages = listOf<MessageData>(
        MessageData("Hola", true),
        MessageData("Mundo", true),
        MessageData("Hola", false),
    )
    Scaffold(
        topBar = {
            TopBar()
        },
        content = {
            Messages(messages = messages)
        },
        bottomBar = {
            InputBar()
        }
    )
}

@Composable
fun Messages(messages: List<MessageData>) {
    LazyColumn {
        items(messages) {message ->
            Message(content = message)
        }
    }
}

@Composable
fun Message(content: MessageData) {

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.primary),
    ) {
        Text(
            text = content.content,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = "ELLA",
                style = MaterialTheme.typography.h1,
            )
        },
        elevation = 12.dp,
        actions = {},
        navigationIcon = {}
    )
}

@Composable
fun InputBar () {
    val text = remember {
        mutableStateOf(TextFieldValue())
    }
    TextField(value = text.value, onValueChange = {
        text.value = it
    })
}
