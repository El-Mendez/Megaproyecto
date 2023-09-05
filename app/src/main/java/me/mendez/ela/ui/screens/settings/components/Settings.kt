package me.mendez.ela.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import kotlinx.coroutines.launch
import me.mendez.ela.settings.ElaSettings

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsStore: DataStore<ElaSettings>,
    startVpn: (Boolean) -> Unit,
    onUpdate: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val elaSettings = settingsStore.data.collectAsState(initial = ElaSettings.default()).value

    Column(modifier) {
        SettingItem(
            title = "Activar Protección",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = elaSettings.vpnRunning,
            onToggle = { startVpn(it) },
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
                        onUpdate()
                        old.copy(blockDefault = it)
                    }
                }
            },
        )
    }
}

