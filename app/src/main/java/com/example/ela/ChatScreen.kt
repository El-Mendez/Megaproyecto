package com.example.ela

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.model.MessageData
import com.example.ela.remote.ChatApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    val messages: SnapshotStateList<MessageData> = mutableStateListOf()
    val calculatingResponse = mutableStateOf(false)

    fun sendMessage(content: String, api: ChatApi) {
        messages.add(
            MessageData(
                content,
                userCreated = true,
                date = Date()
            )
        )

        viewModelScope.launch {
            calculatingResponse.value = true
            try {
                val res = api.getResponse(messages)
                messages.addAll(res)
            } catch (e: Exception) {
                messages.add(
                    MessageData(
                        "Vaya, parece que no tienes conexión a internet",
                        false,
                        Date(),
                    )
                )
            }
            calculatingResponse.value = false
        }
        Log.d("ChatScreen", "sending message :$content")
    }
}

@Composable
fun ChatScreen(
    messages: List<MessageData>,
    calculatingResponse: Boolean,
    onSubmit: (String) -> Unit,
    onReturn: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            ChatTopBar(onReturn)
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
                    WrittingBubble()
                }
            }
        }
    }
}

@Composable
fun MessageBubble(content: MessageData) {
    var showDetails by remember { mutableStateOf(false) }
    val alignment: Alignment
    val backgroundColor: Color
    val fontColor: Color

    val date by remember {
        derivedStateOf {
            val dateFormatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
            dateFormatter.format(content.date)
        }
    }

    if (content.userCreated) {
        alignment = Alignment.CenterEnd
        backgroundColor = MaterialTheme.colors.secondary
        fontColor = MaterialTheme.colors.onSecondary
    } else {
        alignment = Alignment.CenterStart
        backgroundColor = MaterialTheme.colors.primary
        fontColor = MaterialTheme.colors.onPrimary
    }

    Box(
        contentAlignment = alignment,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(8.dp),
            elevation = 5.dp,
            modifier = Modifier
                .padding(vertical = 5.dp, horizontal = 16.dp)
                .clickable { showDetails = !showDetails },
        ) {
            Column {
                Text(
                    text = content.content,
                    style = MaterialTheme.typography.body1.plus(TextStyle(color = fontColor)),
                    modifier = Modifier
                        .padding(6.dp)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showDetails
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.caption,
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun WrittingBubble() {
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
            modifier = Modifier.padding(horizontal = 6.dp).offset(y = 5.dp)
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

@Composable
fun ChatTopBar(onReturn: (() -> Unit)?) {
    TopAppBar(
        title = {
            Text(
                text = "ELA",
                style = MaterialTheme.typography.h2,
            )
        },
        elevation = 12.dp,
        actions = {},
        navigationIcon = {
            if (onReturn == null) return@TopAppBar

            IconButton(onClick = onReturn) {
                Icon(Icons.Filled.ArrowBack, "backIcon")
            }
        }
    )
}

@Composable
fun InputBar(onSubmit: (String) -> Unit, submitEnabled: Boolean) {
    var text by remember { mutableStateOf("") }
    val isNotEmpty by remember { derivedStateOf { text.isNotBlank() } }

    fun sendMessage() {
        if (isNotEmpty && submitEnabled) {
            onSubmit(text)
            text = ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
    ) {
        TextField(
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = { sendMessage() }
            ),
            value = text,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onValueChange = {
                text = it.ifBlank { "" }
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
                    enabled = isNotEmpty && submitEnabled,
                    onClick = { sendMessage() },
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
                        visible = isNotEmpty,
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