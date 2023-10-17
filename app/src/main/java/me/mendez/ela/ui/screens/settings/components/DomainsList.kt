package me.mendez.ela.ui.screens.settings.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DomainsList(modifier: Modifier, domains: List<String>, onUpdate: (List<String>) -> Unit) {
    val lazyListState = rememberLazyListState()
    val currentDomains = remember(domains) {
        mutableStateListOf(*domains.toTypedArray())
    }

    val changed by remember(domains) {
        derivedStateOf {
            currentDomains.size != domains.size || !currentDomains.containsAll(domains)
        }
    }

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
                items = currentDomains,
                key = { _, item -> item.hashCode() }
            ) { index, domain ->
                DomainBlock(
                    domain,
                    index != 0,
                    onDelete = {
                        currentDomains.removeAt(index)
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
                changed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    onClick = {
                        onUpdate(currentDomains.toList())
                    },
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Icon(Icons.Filled.Done, "guardar cambios")
                }
            }

            AnimatedAddInput {
                if (!currentDomains.contains(it)) {
                    currentDomains.add(it)
                    currentDomains.sort()
                }
            }

        }
    }
}
