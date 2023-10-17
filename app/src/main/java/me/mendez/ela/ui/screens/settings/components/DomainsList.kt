package me.mendez.ela.ui.screens.settings.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.mendez.ela.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DomainsList(
    modifier: Modifier,
    domains: List<String>,
    showSave: Boolean,
    onSave: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Box(
        contentAlignment = Alignment.BottomEnd,
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            itemsIndexed(
                items = domains,
                key = { _, item -> item.hashCode() }
            ) { index, domain ->
                DomainBlock(
                    domain,
                    index != 0,
                    onDelete = {
                        onDelete(index)
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(150.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            AnimatedVisibility(
                showSave,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    onClick = {
                        onSave()
                    },
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.save_24),
                        contentDescription = "dudas",
                        tint = MaterialTheme.colors.onPrimary,
                    )
                }
            }

            AnimatedAddInput { onAdd(it) }

        }
    }
}
