package me.mendez.ela.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenericSetting(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isEnabled) {
        MaterialTheme.colors.onBackground
    } else {
        MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clickable { if (isEnabled) onClick() }
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Text(text = title, style = MaterialTheme.typography.h2, color = color)
        Text(
            modifier = Modifier.width(250.dp),
            text = text, style = MaterialTheme.typography.caption,
            color = color,
        )
    }
}
