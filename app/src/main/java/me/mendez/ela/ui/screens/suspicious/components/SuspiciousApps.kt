package me.mendez.ela.ui.screens.suspicious.components

import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuspiciousApps(packagesNames: List<String>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val apps: List<AppData> by produceState(listOf()) {
        return@produceState withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val apps = mutableListOf<AppData>()

            packageManager
                .getInstalledApplications(PackageManager.MATCH_ALL)
                .forEach {
                    if (!packagesNames.contains(it.packageName)) return@forEach

                    val icon = it.loadIcon(packageManager)
                    val name = it.loadLabel(packageManager).toString()

                    apps.add(AppData(it.packageName, name, icon))
                }

            value = apps
        }
    }

    Box(modifier) {
        LazyColumn {
            itemsIndexed(apps) { index, app ->
                SuspiciousApp(
                    app,
                    index != 0,
                    Modifier.animateItemPlacement()
                )
            }
        }
    }
}
