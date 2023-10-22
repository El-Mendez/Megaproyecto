package me.mendez.ela.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.mendez.ela.R
import me.mendez.ela.persistence.database.blocks.DailyBlocks

@Composable
fun DailyBlocksScreen(onReturn: (() -> Unit)?, dailyBlocks: List<DailyBlocks>) {
    Scaffold(
        topBar = {
            DailyBlocksTopBar(onReturn)
        },
        content = {
            DailyBlocksList(modifier = Modifier.padding(it), dailyBlocks)
        }
    )
}

@Composable
fun DailyBlocksList(modifier: Modifier = Modifier, dailyBlocks: List<DailyBlocks>) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            dailyBlocks.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No has tenido ningún tráfico sospechoso hoy",
                    style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_sentiment_very_satisfied_96),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                )
            }
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 10.dp)) {
            itemsIndexed(dailyBlocks) { index, block ->
                if (index != 0) {
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    )
                }

                Column(
                    modifier = modifier
                        .padding(vertical = 15.dp, horizontal = 15.dp)
                ) {
                    Text(
                        block.domain,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (block.amount == 1) "1 bloqueo" else "${block.amount} bloqueos",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

@Composable
fun DailyBlocksTopBar(onReturn: (() -> Unit)?) {
    TopAppBar(
        title = {
            Text(
                text = "Tráfico Web",
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
