package com.example.ela

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ela.model.AppBlock
import com.example.ela.ui.theme.ElaTheme

@Composable
fun MainScreen(appBlockList: List<AppBlock>) {
    val isOpen = remember { mutableStateOf(true) }
    ElaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                Text(text = "Ela", style = MaterialTheme.typography.h1)
                AppBlockList(appBlockList)
            }
            if (isOpen.value) {
                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text(
                            text = "¡Cuidado con el phishing!",
                            style = MaterialTheme.typography.h2,
                        )
                    },
                    text = {
                        Text(
                            text = "Antes de entrar a cualquier link, asegúrate de que el enlace apunta a un sitio web legítimo y confiable",
                            style = MaterialTheme.typography.body1,
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { isOpen.value = false }) {
                            Text(text = "Ok")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppBlockList(appBlockList: List<AppBlock>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(appBlockList) {appBlock ->
            AppBlockCard(
                appBlock,
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )
            Divider(
                color = Color.LightGray,
                startIndent = 120.dp,
                thickness = 1.dp,
            )
        }
    }
}

@Composable
fun AppBlockCard(appBlock: AppBlock, modifier: Modifier = Modifier.fillMaxWidth(1f)) {
    Surface(
        modifier = modifier
    ) {
        Row {
            Spacer(
                modifier = Modifier
                    .width(115.dp)
                    .height(100.dp)
                    .padding(start = 15.dp)
                    .drawWithContent {
                        drawIntoCanvas { canvas ->
                            appBlock.appImage.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                            appBlock.appImage.draw(canvas.nativeCanvas)
                        }
                    }
            )
//            Image(
//                painter = painterResource(id = appBlock.appImage),
//                contentDescription = appBlock.name,
//                modifier = Modifier
//                    .width(100.dp)
//                    .height(100.dp)
//                    .padding(start = 15.dp),
//                contentScale = ContentScale.Fit
//            )
            Text(
                text = appBlock.name,
                style = MaterialTheme.typography.h2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 15.dp, top = 30.dp)
                    .width(180.dp)
//                    .width()
            )
            Box(
                modifier = Modifier
                    .padding(top = 30.dp, end = 15.dp)
                    .fillMaxSize(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(25.dp)
                        .width(25.dp)
                        .height(25.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${appBlock.blocks.size}",
                        color = MaterialTheme.colors.onPrimary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBlockCardPreview() {
    ElaTheme {
//        AppBlockCard(appBlock = AppBlock(
//            "Samsung Internet",
//            listOf(InternetBlock("1h", "virus.com", listOf("Phishing"))),
//            R.drawable.samsung_internet
//        ))
    }
}

