package me.mendez.ela.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.mendez.ela.R

@Composable
fun MainScreen(onSend: () -> Unit, onSettings: () -> Unit, onDetails: () -> Unit) {
    val image = ImageBitmap.imageResource(R.drawable.background_tile)
    val brush = remember {
        ShaderBrush(
            ImageShader(
                image,
                TileMode.Repeated,
                TileMode.Repeated,
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        TopButtons(onSend, onSettings)

        Score(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth(),
            score = 10,
        )

        DailyTip("Cuidado con el Phishing", onSend)

        DailyBlocksCard(onDetails)
    }
}

@Composable
fun Score(score: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        var animationStarted by remember { mutableStateOf(false) }
        val animatedScore by animateIntAsState(
            targetValue = if (animationStarted) score else 0,
            animationSpec = tween(5000)
        )

        LaunchedEffect(Unit) {
            animationStarted = true
        }

        Text(
            animatedScore.toString(),
            style = MaterialTheme.typography.h1.copy(
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            "Tu puntaje de hábitos de Seguridad",
            style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onBackground)
        )
    }
}

@Composable
fun TopButtons(onSendAction: () -> Unit, onSettingsActions: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(
            onClick = onSendAction,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.round_send_24),
                contentDescription = "dudas",
                tint = MaterialTheme.colors.primary,
            )
        }
        Box {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "más opciones",
                    tint = MaterialTheme.colors.onBackground
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                },
            ) {
                DropdownMenuItem(onClick = onSettingsActions) {
                    Text("Ajustes")
                }
            }
        }
    }
}

@Composable
fun DailyTip(content: String, onClick: () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                onClick()
            },
        elevation = 5.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Consejo del día:",
                style = MaterialTheme.typography.caption.copy(
                    color = MaterialTheme.colors.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = content,
                style = MaterialTheme.typography.h2.copy(color = MaterialTheme.colors.onPrimary),
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
fun CardWithTitle(
    top: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                onClick()
            },
        elevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                top(this)

                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }

            Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

            content(this)
        }

    }
}

@Composable
fun DailyBlocksCard(onClick: () -> Unit) {
    CardWithTitle(
        top = {
            Column {
                Text(
                    text = "15",
                    style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold),
                )
                Text(text = "tráfico sospechoso bloqueado hoy.", style = MaterialTheme.typography.caption)
            }
        },
        onClick = onClick,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                repeat(3) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(45.dp)
                                .width(45.dp)
                                .padding(end = 10.dp, top = 5.dp, bottom = 5.dp)
                                .background(Color.Black)
                        )
                        Column {
                            Text("Samsung Internet", fontWeight = FontWeight.Bold)
                            Text("3 bloqueos", style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
    )
}
