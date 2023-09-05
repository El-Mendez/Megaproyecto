package me.mendez.ela.ui.screens.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.mendez.ela.R

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
