package me.mendez.ela.ui.screens.suspicious

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.mendez.ela.ui.general.PopBackTopBar
import me.mendez.ela.ui.screens.suspicious.components.SuspiciousApps


@Composable
fun SuspiciousAppsScreen(
    packagesNames: List<String>?,
    onReturn: (() -> Unit),
) {
    Scaffold(
        topBar = {
            PopBackTopBar("Aplicaciones sospechosas", onReturn)
        },
        content = {
            SuspiciousApps(
                packagesNames,
                modifier = Modifier.padding(it),
            )
        }
    )
}
