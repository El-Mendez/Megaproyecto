package me.mendez.ela.ui.general

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PopBackTopBar(title: String, onReturn: (() -> Unit)?) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h2,
            )
        },
        elevation = 12.dp,
        navigationIcon = {
            if (onReturn == null) return@TopAppBar

            IconButton(onClick = onReturn) {
                Icon(Icons.Filled.ArrowBack, "regresar")
            }
        }
    )
}
