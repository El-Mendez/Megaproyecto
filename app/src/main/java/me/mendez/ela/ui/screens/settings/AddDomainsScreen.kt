package me.mendez.ela.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.mendez.ela.R
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
    val showEmpty by remember(settings.whitelist) {
        derivedStateOf {
            currentDomains.isEmpty()
        }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    showEmpty,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No tienes ningún dominio permitido todavía",
                            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_search_96),
                            contentDescription = null,
                            tint = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        )
                    }
                }
            }

            DomainsList(
                modifier = Modifier.padding(it),
                domains = currentDomains,
                showSave = changed && settings.ready,
                onSave = {
                    if (!settings.ready) return@DomainsList

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
