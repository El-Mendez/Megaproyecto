package me.mendez.ela.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.mendez.ela.R
import me.mendez.ela.persistence.database.blocks.DailyBlocks
import java.util.Calendar
import kotlin.random.Random


@Composable
fun MainScreen(
    onSend: () -> Unit,
    onSettings: () -> Unit,
    onDetails: () -> Unit,
    vpnEnabled: Boolean,
    enableVpn: () -> Unit,
    suspiciousAppsAmount: Int,
    onSuspiciousAppClick: () -> Unit,
    totalBlocks: Int,
    dailyBlocks: List<DailyBlocks>,
    totalDailyBlocks: Int,
) {
    val image = ImageBitmap.imageResource(R.drawable.background_tile)
    val verticalScroll = rememberScrollState()
    val tip = remember {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        TIPS
            .toMutableList()
            .shuffled(Random(month))
            .get(day % TIPS.size)
    }

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
            .verticalScroll(verticalScroll)
    ) {
        TopButtons(onSend, onSettings)

        Score(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth(),
            blocks = totalBlocks,
        )

        AnimatedVisibility(suspiciousAppsAmount != 0) {
            SuspiciousAppsWarning(
                onSuspiciousAppClick,
                suspiciousAppsAmount
            )
        }

        AnimatedVisibility(!vpnEnabled) {
            DisabledWarning(enableVpn)
        }

        DailyTip(tip, onSend)


        if (dailyBlocks.isEmpty()) {
            EmptyDailyBlocksCard()
        } else {
            DailyBlocksCard(onDetails, dailyBlocks, totalDailyBlocks)
        }
    }
}

@Composable
fun Score(blocks: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {

        var animationStarted by remember { mutableStateOf(false) }
        val animatedScore by animateIntAsState(
            targetValue = if (animationStarted) blocks else 0,
            animationSpec = tween(500),
        )

        LaunchedEffect(Unit) {
            animationStarted = true
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(25.dp)
                .clip(RoundedCornerShape(7.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                animatedScore.toString(),
                style = MaterialTheme.typography.h1.copy(
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                "veces que Ela te ha protegido",
                style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onBackground)
            )
        }
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
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
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
fun DailyBlocksCard(onClick: () -> Unit, dailyBlocks: List<DailyBlocks>, totalDailyBlocks: Int) {
    val topBlocks = dailyBlocks.slice(0..<minOf(3, dailyBlocks.size))

    CardWithTitle(
        top = {
            Column {
                Text(
                    text = totalDailyBlocks.toString(),
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
                topBlocks.map { block ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(55.dp)
                                .width(55.dp)
                                .padding(end = 10.dp, top = 5.dp, bottom = 5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (block.domain.firstOrNull() ?: "").toString().uppercase(),
                                color = MaterialTheme.colors.onSecondary,
                                style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold),
                            )
                        }
                        Column {
                            Text(block.domain, fontWeight = FontWeight.Bold)
                            Text(
                                if (block.amount == 1) "1 bloqueo" else "${block.amount} bloqueos",
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyDailyBlocksCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No has tenido ningún tráfico sospechoso hoy",
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
}

@Composable
fun DisabledWarning(onClick: () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.colors.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
        elevation = 10.dp,
    ) {
        Text(
            text = "No estás protegido por Ela",
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.onSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
    }
}

@Composable
fun SuspiciousAppsWarning(onClick: () -> Unit, amount: Int) {
    val name = if (amount == 1) {
        "Tienes una aplicación sospechosa"
    } else {
        "Tienes $amount aplicaciones sospechosas"
    }

    Card(
        backgroundColor = Color.Red,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
        elevation = 10.dp,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.h2,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
    }
}

val TIPS = arrayOf(
    "Usa contraseñas fuertes",
    "Habilita segundo factor de autenticación (2FA)",
    "Actualiza tus dispositivos",
    "Utiliza un antivirus",
    "Siempre ten copias de seguridad actualizadas",
    "¡No uses la contraseña default del Wifi!",
    "No compartas información personal en línea",
    "Solo descarga aplicaciones de fuentes confiables",
    "No reutilices contraseñas",
    "Ten cuidado al conectar USBs desconocidas",
    "Educa a tu familia y conocidos",
    "Usa un administrador de contraseñas",
    "No dés información personal por teléfono",
    "Bloquea tu computadora cada vez que te alejes de ella",
    "No compartas contraseñas",
    "Evita usar redes wifis públicas",
    "Desinstala aplicaciones que ya no uses",
    "Usa autenticación biométrica",
    "Cubre tu cámara web mientras no la utilices",
    "Ten un correo solo para cosas importantes",
    "Ten cuidado al descargar archivos adjuntos en correos",
    "Evita darle clic a anuncios publicitarios",
    "Utiliza un bloqueador de anuncios",
    "Desconfía de correos sospechoso",
    "No compartas información importante por teléfono",
)
