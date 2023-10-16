package me.mendez.ela.ui.screens.suspicious

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AppData(
    val packageName: String,
    val name: String,
    val image: Drawable
)

@Composable
fun SuspiciousApp(app: AppData) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${app.packageName}"),
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launcher.launch(
                    intent
                )
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
                        app.image.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                        app.image.draw(canvas.nativeCanvas)
                    }
                }
        )
        Spacer(modifier = Modifier.width(20.dp))

        Text(text = app.name, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
    }

    Divider(
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colors.surface,
    )

}
