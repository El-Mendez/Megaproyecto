package com.example.ela

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.grid.LazyGridItemScopeImpl.animateItemPlacement
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class MessageData(
    val content: String,
    val userCreated: Boolean,
)

@Composable
@Preview
fun ChatScreen() {
    val messages = remember {
        mutableStateListOf(
            MessageData("Hola", true),
            MessageData("Mundo", true),
            MessageData("Hola", false),
            MessageData("Hola", true),
            MessageData("Mundo", true),
            MessageData("Hola", false),
            MessageData("Hola", true),
            MessageData("Mundo", true),
            MessageData("Hola", false),
            MessageData("Hola", true),
            MessageData("Mundo", true),
            MessageData("Hola", true),
            MessageData("Mundo", true),
            MessageData("Hola", false),
            MessageData("Hola", false),
        )
    }

    Scaffold(
        topBar = {
            TopBar()
        },
        content = {
            Messages(
                messages = messages,
                modifier = Modifier.padding(it),
            )
        },
        bottomBar = {
            InputBar(
                onSubmit = {
                    messages.add(
                        MessageData(
                            it,
                            true,
                    ))
                    println("SUBMITTED $it")
                }
            )
        }
    )
}

@Composable
fun Messages(messages: List<MessageData>, modifier: Modifier) {
    val lazyListState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
    ) {
        coroutine.launch {
            lazyListState.scrollToItem(messages.size - 1)
        }

        items(messages) {message ->
            Message(content = message)
        }
    }
}

@Composable
fun Message(content: MessageData) {
    if (content.userCreated) {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Surface(
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(8.dp),
                elevation = 5.dp,
                modifier = Modifier
                    .padding(vertical = 5.dp, horizontal = 16.dp),
            ) {
                Text(
                    text = content.content,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(6.dp)
                )
            }
        }
    } else {
        Surface(
            color = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(vertical = 5.dp, horizontal = 16.dp),
            elevation = 5.dp,
        ) {
            Text(
                text = content.content,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(6.dp)
            )
        }
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
fun InputBar (onSubmit: (String) -> Unit) {
    val text = remember { mutableStateOf("a") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
    ) {
        TextField(
            value = text.value,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onValueChange = {
                if (it.isNotBlank())
                    text.value = it
                else
                    text.value = ""
            },
            placeholder = {
                Text(text = "Escribe tus dudas de ciberseguridad...")
            }
        )

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp),
            ) {
                Button(
                    enabled = text.value.isNotBlank(),
                    onClick = {
                        onSubmit(text.value)
                        text.value = ""
                    },
                    modifier = Modifier.size(56.dp),
                    elevation = ButtonDefaults.elevation(0.dp),
                    colors = ButtonDefaults
                        .buttonColors(
                            backgroundColor = Color.Transparent,
                            disabledBackgroundColor = Color.Transparent
                        ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = text.value.isNotBlank(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_send_24),
                            contentDescription = "Enviar",
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                }
            }
        }
    }
}