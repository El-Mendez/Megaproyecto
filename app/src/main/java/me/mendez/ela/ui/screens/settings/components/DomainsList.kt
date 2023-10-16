package me.mendez.ela.ui.screens.settings.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.mendez.ela.R
import me.mendez.ela.ui.screens.suspicious.components.AppData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DomainsList(modifier: Modifier, domains: List<String>, onUpdate: (List<String>) -> Unit) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        itemsIndexed(
            items = domains,
            key = { _, item -> item }
        ) { index, domain ->
            DomainBlock(
                domain,
                index != 0,
                {},
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
fun DomainBlock(name: String, divider: Boolean, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        if (divider) {
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    "borrar",
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    }
}
