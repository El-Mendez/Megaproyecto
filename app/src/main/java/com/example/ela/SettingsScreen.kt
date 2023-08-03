package com.example.ela

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onReturn: (() -> Unit)?, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Scaffold(
        topBar = {
            SettingsTopBar(onReturn)
        },
        content = {
            Settings(
                modifier = Modifier.padding(it),
                isEnabled = isEnabled,
                onToggle = onToggle,
            )
        }
    )
}

@Composable
fun Settings(modifier: Modifier = Modifier, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(modifier) {
        SettingItem(
            title = "Bloquear conexiones",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isEnabled = isEnabled,
            onToggle = onToggle,
        )
    }
}

@Composable
fun SettingItem(modifier: Modifier = Modifier, title: String, text: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.h2)
                Text(
                    modifier = Modifier.width(250.dp),
                    text = text, style = MaterialTheme.typography.caption
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
fun SettingsTopBar(onReturn: (() -> Unit)?) {
    TopAppBar(
        title = {
            Text(
                text = "Ajustes",
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
