package me.mendez.ela.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DomainBlock(name: String, divider: Boolean, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        if (divider) {
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    "borrar",
                    tint = MaterialTheme.colors.secondary,
                )
            }
        }
    }
}
