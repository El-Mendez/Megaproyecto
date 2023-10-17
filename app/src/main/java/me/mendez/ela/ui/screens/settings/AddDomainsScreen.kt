package me.mendez.ela.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.general.PopBackTopBar
import me.mendez.ela.ui.screens.settings.components.DomainsList

@Composable
fun AddDomainsScreen(
    onReturn: (() -> Unit),
    settings: ElaSettings,
    update: ((ElaSettings) -> ElaSettings) -> Unit,
) {
    Scaffold(
        topBar = {
            PopBackTopBar("Dominios Permitidos", onReturn)
        },
        content = {
            DomainsList(
                modifier = Modifier.padding(it),
                domains = settings.domains,
                onUpdate = { newDomains ->
                    update {
                        it.copy(domains = newDomains)
                    }
                },
            )
        }
    )
}
