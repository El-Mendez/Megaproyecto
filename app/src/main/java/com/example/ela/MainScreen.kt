package com.example.ela

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ela.data.DataSource
import com.example.ela.model.AppBlock
import com.example.ela.model.InternetBlock
import com.example.ela.ui.theme.ElaTheme

@Composable
fun MainScreen() {
    ElaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                Text(text = "Hola Mundo", style = MaterialTheme.typography.h1)
                AppBlockList(appBlockList = DataSource().loadAppBlocks())
            }
        }
    }
}

@Composable
fun AppBlockList(appBlockList: List<AppBlock>) {
    LazyColumn {
        items(appBlockList) {appBlock ->
            AppBlockCard(appBlock)
        }
    }
}

@Composable
fun AppBlockCard(appBlock: AppBlock, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = appBlock.appImage),
                contentDescription = appBlock.name,
                modifier = Modifier
                    .width(60.dp)
                    .padding(end = 16.dp)
                ,
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Se bloqueó ${appBlock.blocks.size} dominios maliciosos de ${appBlock.name}!"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBlockCardPreview() {
    AppBlockCard(appBlock = AppBlock(
        "Samsung Internet",
        listOf(InternetBlock("1h", "virus.com", listOf("Phishing"))),
        R.drawable.samsung_internet
    )
    )
}
