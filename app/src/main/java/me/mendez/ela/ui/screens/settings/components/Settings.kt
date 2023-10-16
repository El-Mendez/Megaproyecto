package me.mendez.ela.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.mendez.ela.persistence.settings.ElaSettings

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settings: ElaSettings,
    update: ((ElaSettings) -> ElaSettings) -> Unit,
) {

    Column(modifier) {
        SettingItem(
            title = "Activar Protección",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = settings.vpnRunning,
            onToggle = {
                update { old ->
                    old.copy(vpnRunning = it)
                }
            },
            isEnabled = true,
        )
        SettingItem(
            title = "Bloquear conexiones",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = settings.blockDefault,
            isEnabled = settings.vpnRunning,
            onToggle = {
                update { old ->
                    old.copy(blockDefault = it)
                }
            },
        )
    }
}
