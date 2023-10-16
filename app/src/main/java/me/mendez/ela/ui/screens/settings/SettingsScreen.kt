package me.mendez.ela.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.general.PopBackTopBar
import me.mendez.ela.ui.screens.settings.components.Settings

@Composable
fun SettingsScreen(
    onReturn: (() -> Unit)?,
    settings: ElaSettings,
    update: ((ElaSettings) -> ElaSettings) -> Unit,
) {
    Scaffold(
        topBar = {
            PopBackTopBar("Ajustes", onReturn)
        },
        content = {
            Settings(
                modifier = Modifier.padding(it),
                settings,
                update,
            )
        }
    )
}
