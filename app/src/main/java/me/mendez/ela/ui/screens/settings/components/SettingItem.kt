package me.mendez.ela.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    isOn: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val color = if (isEnabled) {
        MaterialTheme.colors.onBackground
    } else {
        MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
    }
    Box(
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.h2, color = color)
                Text(
                    modifier = Modifier.width(250.dp),
                    text = text, style = MaterialTheme.typography.caption,
                    color = color,
                )
            }
            Switch(
                checked = isOn,
                onCheckedChange = { if (isEnabled) onToggle(it) },
                enabled = isEnabled,
            )
        }
    }
}

