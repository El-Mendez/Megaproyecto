package me.mendez.ela.ui.screens.suspicious.components

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.mendez.ela.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuspiciousApps(packagesNames: List<String>?, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val apps by produceState<List<AppData>?>(initialValue = null, packagesNames) {
        if (packagesNames == null) {
            value = null
            return@produceState
        }

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

            value = apps.sortedBy { it.name }
        }
    }

    val loading by remember(packagesNames) { derivedStateOf { apps == null } }
    val empty by remember(packagesNames) { derivedStateOf { apps != null && apps!!.isEmpty() } }

    Box(modifier) {
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        AnimatedVisibility(
            empty,
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
                    text = "¡No tienes ninguna aplicación sospechosa!",
                    style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.round_celebration_96),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                )
            }
        }

        LazyColumn {
            itemsIndexed(
                apps ?: emptyList(),
                key = { _, app -> app.packageName }
            ) { index, app ->
                SuspiciousApp(
                    app,
                    index != 0,
                    Modifier.animateItemPlacement()
                )
            }
        }
    }
}
