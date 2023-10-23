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
    onAddDomains: () -> Unit,
    onExport: () -> Unit,
) {
    Column(modifier) {
        SettingItem(
            title = "Activar Protección",
            text = "Ela supervisará tu tráfico red.",
            isOn = settings.vpnRunning,
            onToggle = {
                update { old ->
                    old.copy(vpnRunning = it)
                }
            },
            isEnabled = settings.ready,
        )
        SettingItem(
            title = "Bloquear conexiones",
            text = "Automáticamente bloquear el tráfico tráfico sospechoso.",
            isOn = settings.blockDefault,
            isEnabled = settings.ready,
            onToggle = {
                update { old ->
                    old.copy(blockDefault = it)
                }
            },
        )
        SettingItem(
            title = "Proteger Automáticamente",
            text = "Activa la protección automáticamente al encender el dispositivo.",
            isOn = settings.startOnBoot,
            isEnabled = settings.ready,
            onToggle = {
                update { old ->
                    old.copy(startOnBoot = it)
                }
            },
        )
        GenericSetting(
            title = "Dominios Permitidos",
            text = if (settings.blockDefault) {
                "Páginas que nunca serán bloqueadas"
            } else {
                "Páginas que nunca darán advertencia"
            },
            onClick = onAddDomains,
            isEnabled = settings.ready,
        )
        GenericSetting(
            title = "Exportar datos",
            text = "Exporta únicamente información sobre qué dominios fueron bloqueados.",
            onClick = onExport,
            isEnabled = true,
        )
    }
}
