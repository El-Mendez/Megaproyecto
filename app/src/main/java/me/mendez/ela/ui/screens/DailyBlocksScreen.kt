package me.mendez.ela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var loading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
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
