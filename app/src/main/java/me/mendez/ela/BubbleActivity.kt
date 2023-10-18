package me.mendez.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.screens.chat.ChatScreen
import me.mendez.ela.ui.theme.ElaTheme
import javax.inject.Inject

@AndroidEntryPoint
class BubbleActivity : ComponentActivity() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    @Inject
    lateinit var database: SuspiciousAppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElaTheme {
                ChatScreen(
                    "bubble",
                    emptyList(),
                    true,
                    onSubmit = {},
                    onReturn = null,
                )
            }
        }
    }
}
