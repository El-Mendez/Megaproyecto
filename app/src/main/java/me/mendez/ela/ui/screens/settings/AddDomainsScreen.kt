package me.mendez.ela.ui.screens.settings

import androidx.activity.compose.BackHandler
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
    var currentDomains: List<String> by remember(settings.whitelist) {
        mutableStateOf(settings.whitelist.toMutableList())
    }

    val changed by remember(settings.whitelist) {
        derivedStateOf {
            currentDomains.size != settings.whitelist.size || !currentDomains.containsAll(settings.whitelist)
        }
    }

    var alertOpened by remember {
        mutableStateOf(false)
    }

    val tryReturn = {
        if (changed && !alertOpened) {
            alertOpened = true
        } else {
            onReturn()
        }
    }

    BackHandler(onBack = tryReturn)

    if (alertOpened) {
        AlertDialog(
            onDismissRequest = { alertOpened = false },
            title = {
                Text(text = "¿Estás seguro?")
            },
            text = {
                Text(text = "Perderás todos tus cambios")
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { alertOpened = false }
                    ) {
                        Text(text = "Cancelar")
                    }
                    TextButton(
                        onClick = { onReturn() }
                    ) {
                        Text(text = "Ok")
                    }
                }
            }

        )
    }

    Scaffold(
        topBar = {
            PopBackTopBar("Dominios Permitidos", tryReturn)
        },
        content = {
            DomainsList(
                modifier = Modifier.padding(it),
                domains = currentDomains,
                showSave = changed,
                onSave = {
                    if (changed) {
                        update { old ->
                            old.withWhitelist(currentDomains)
                        }
                    }
                },
                onAdd = { newDomain ->
                    if (!currentDomains.contains(newDomain)) {
                        currentDomains = currentDomains
                            .toMutableList()
                            .apply {
                                add(newDomain)
                                sort()
                            }
                    }
                },
                onDelete = {
                    currentDomains = currentDomains
                        .toMutableList()
                        .apply {
                            removeAt(it)
                        }
                }
            )
        }
    )
}
