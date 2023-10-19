package me.mendez.ela.ui.screens.suspicious.components

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
fun SuspiciousApp(app: AppData, divider: Boolean, modifier: Modifier = Modifier) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

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
                .padding(vertical = 10.dp, horizontal = 20.dp),
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

            Text(
                text = app.name,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 20.dp)
            )
        }
    }
}
