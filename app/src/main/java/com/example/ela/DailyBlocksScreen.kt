package com.example.ela

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ela.model.AppBlock

@Composable
fun DailyBlocksScreen(onReturn: (() -> Unit)?) {
    Scaffold (
        topBar = {
            DailyBlocksTopBar(onReturn)
        },
        content = {
            DailyBlocks(modifier = Modifier.padding(it))
        }
    )
}

@Composable
fun DailyBlocks(modifier: Modifier = Modifier) {
    var apps: List<AppBlock> by remember { mutableStateOf(listOf()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val packageManager = context.packageManager

        val allApps = packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
        val appBlocks = mutableListOf<AppBlock>()

        allApps.forEach {
            val icon = it.loadIcon(packageManager)
            val name = it.loadLabel(packageManager).toString()

            appBlocks.add(AppBlock(name, listOf(), icon))
        }

        apps = appBlocks
    }

    Box(modifier = modifier) {
        LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
            items(apps) { app ->
                Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .drawWithContent {
                                drawIntoCanvas { canvas ->
                                    app.appImage.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                                    app.appImage.draw(canvas.nativeCanvas)
                                }
                            }
                    )
                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(text = app.name, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                        Text(text = "${app.blocks.size} bloqueos hoy.", style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyBlocksTopBar(onReturn: (() -> Unit)?) {
    TopAppBar(
        title = {
            Text(
                text = "Tráfico Web",
                style = MaterialTheme.typography.h1,
            )
        },
        elevation = 12.dp,
        actions = {},
        navigationIcon = {
            if (onReturn == null) return@TopAppBar

            IconButton(onClick = onReturn) {
                Icon(Icons.Filled.ArrowBack, "backIcon")
            }
        }
    )
}
