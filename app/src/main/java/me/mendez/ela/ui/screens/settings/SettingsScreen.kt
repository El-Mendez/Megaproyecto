package me.mendez.ela.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.ui.screens.settings.components.Settings
import me.mendez.ela.ui.screens.settings.components.SettingsTopBar

@Composable
fun SettingsScreen(
    onReturn: (() -> Unit)?,
    settingsStore: DataStore<ElaSettings>,
    startVpn: (Boolean) -> Unit,
    onUpdate: () -> Unit,
) {
    Scaffold(
        topBar = {
            SettingsTopBar(onReturn)
        },
        content = {
            Settings(
                modifier = Modifier.padding(it),
                settingsStore,
                startVpn,
                onUpdate,
            )
        }
    )
}


