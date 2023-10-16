package me.mendez.ela.ui.screens.suspicious

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun SuspiciousAppsScreen(
    packagesNames: List<String>,
    onReturn: (() -> Unit),
) {
    Scaffold(
        topBar = {
            SuspiciousTopBar(onReturn)
        },
        content = {
            SuspiciousApps(
                packagesNames,
                modifier = Modifier.padding(it),
            )
        }
    )
}
