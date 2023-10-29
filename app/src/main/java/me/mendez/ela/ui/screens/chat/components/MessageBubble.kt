package me.mendez.ela.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import me.mendez.ela.chat.Sender
import java.text.DateFormat

@Composable
fun MessageBubble(content: Message) {
    var showDetails by remember { mutableStateOf(false) }
    val arrangement: Arrangement.Horizontal
    val backgroundColor: Color
    val fontColor: Color
    val shape: RoundedCornerShape
    val columnAlignment: Alignment.Horizontal

    val date by remember {
        derivedStateOf {
            val dateFormatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
            dateFormatter.format(content.date)
        }
    }

    if (content.user == Sender.USER) {
        arrangement = Arrangement.End
        backgroundColor = MaterialTheme.colors.secondary
        fontColor = MaterialTheme.colors.onSecondary
        shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp)
        columnAlignment = Alignment.End
    } else {
        arrangement = Arrangement.Start
        backgroundColor = MaterialTheme.colors.primary
        fontColor = MaterialTheme.colors.onPrimary
        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp)
        columnAlignment = Alignment.Start
    }

    Row(
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
    ) {
        if (content.user != Sender.USER) {
            Box(
                modifier = Modifier.background(
                    color = backgroundColor,
                    shape = TriangleEdgeShape(16, false)
                )
                    .width(16.dp)
                    .height(16.dp)
            )
        }

        Surface(
            color = backgroundColor,
            shape = shape,
            elevation = 5.dp,
            modifier = Modifier
                .clickable { showDetails = !showDetails },
        ) {
            Column(
                horizontalAlignment = columnAlignment
            ) {
                Text(
                    text = content.content,
                    style = MaterialTheme.typography.body1.plus(TextStyle(color = fontColor)),
                    modifier = Modifier
                        .padding(6.dp)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showDetails,
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                }
            }
        }
        if (content.user == Sender.USER) {
            Box(
                modifier = Modifier.background(
                    color = backgroundColor,
                    shape = TriangleEdgeShape(16, true)
                )
                    .width(16.dp)
                    .height(16.dp)
            )
        }
    }
}
