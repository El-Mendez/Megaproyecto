package me.mendez.ela.ui.screens.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.mendez.ela.chat.Message
import java.text.DateFormat

@Composable
fun MessageBubble(content: Message) {
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
