package me.mendez.ela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import kotlinx.coroutines.launch
import me.mendez.ela.settings.ElaSettings

@Composable
fun SettingsScreen(onReturn: (() -> Unit)?, settingsStore: DataStore<ElaSettings>) {
    Scaffold(
        topBar = {
            SettingsTopBar(onReturn)
        },
        content = {
            Settings(
                modifier = Modifier.padding(it),
                settingsStore,
            )
        }
    )
}

@Composable
fun Settings(modifier: Modifier = Modifier, settingsStore: DataStore<ElaSettings>) {
    val scope = rememberCoroutineScope()
    val elaSettings = settingsStore.data.collectAsState(initial = ElaSettings.default()).value

    Column(modifier) {
        SettingItem(
            title = "Activar Protección",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = elaSettings.vpnRunning,
            onToggle = {
                scope.launch {
                    settingsStore.updateData { old ->
                        old.copy(vpnRunning = it)
                    }
                }
            },
            isEnabled = true,
        )
        SettingItem(
            title = "Bloquear conexiones",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = elaSettings.blockDefault,
            isEnabled = elaSettings.vpnRunning,
            onToggle = {
                scope.launch {
                    settingsStore.updateData { old ->
                        old.copy(blockDefault = it)
                    }
                }
            },
        )
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    isOn: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val color = if (isEnabled) {
        MaterialTheme.colors.onBackground
    } else {
        MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
    }
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
                Text(text = title, style = MaterialTheme.typography.h2, color = color)
                Text(
                    modifier = Modifier.width(250.dp),
                    text = text, style = MaterialTheme.typography.caption,
                    color = color,
                )
            }
            Switch(
                checked = isOn,
                onCheckedChange = { if (isEnabled) onToggle(it) },
                enabled = isEnabled,
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
