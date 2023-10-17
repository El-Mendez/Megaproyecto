package me.mendez.ela.ui.screens.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun getHost(url: String): String? {
    if (url.matches("([a-zA-Z0-9\\-_]+:\\/\\/)?([a-zA-Z0-9\\-_]+\\.)*([a-zA-Z0-9\\-_]+@)?[a-zA-Z0-9\\-_]+\\.[a-zA-Z0-9\\-_]+(:[0-9]+)?[\\/@a-zA-Z0-9%&=+]*".toRegex())) {
        return url
            .split("://", limit = 2).last()
            .split("@", limit = 2).last()
            .split("/", limit = 2).first()
            .split(":", limit = 2).first()
    }
    return null
}


@Composable
fun AnimatedAddInput(onAdd: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    var text by remember {
        mutableStateOf("")
    }
    var adding by remember {
        mutableStateOf(false)
    }
    val isValid by remember {
        derivedStateOf {
            !getHost(text).isNullOrBlank()
        }
    }


    LaunchedEffect(focused) {
        adding = focused
    }

    val transition = updateTransition(adding, label = "transition")
    val containerColor by transition.animateColor(
        transitionSpec = {
            if (true isTransitioningTo false) {
                tween(800)
            } else {
                tween(300)
            }
        },
        targetValueByState = {
            if (it) MaterialTheme.colors.primary else Color.Transparent
        },
        label = "color"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(containerColor)
            .padding(8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        AnimatedVisibility(adding) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f),
                interactionSource = interactionSource,
                colors = TextFieldDefaults
                    .textFieldColors(
                        textColor = MaterialTheme.colors.onPrimary,
                        backgroundColor = Color.Transparent,
                        cursorColor = MaterialTheme.colors.onBackground,
                    ),
                placeholder = {
                    Text(
                        text = "ejemplo.com",
                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)
                    )
                }
            )
        }


        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, RoundedCornerShape(15.dp), clip = false)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colors.secondary)
                .clickable {
                    if (!adding) {
                        adding = true
                    } else if (!isValid) {
                        adding = false
                    } else {
                        val host = getHost(text) ?: return@clickable
                        text = ""
                        onAdd(host)
                    }
                }
                .size(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !adding,
            ) {
                Icon(
                    Icons.Filled.Create,
                    "añadir dominio",
                    tint = MaterialTheme.colors.onSecondary,
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = adding && !isValid,
            ) {
                Icon(
                    Icons.Filled.Close,
                    "añadir dominio",
                    tint = MaterialTheme.colors.onSecondary,
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = adding && isValid,
            ) {
                Icon(
                    Icons.Filled.Add,
                    "añadir dominio",
                    tint = MaterialTheme.colors.onSecondary,
                )
            }
        }
    }
}
