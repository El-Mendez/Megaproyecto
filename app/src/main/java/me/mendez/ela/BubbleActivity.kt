package me.mendez.ela

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.screens.chat.ChatScreen
import me.mendez.ela.ui.theme.ElaTheme
import javax.inject.Inject

private const val TAG = "ELA_BUBBLE"

@AndroidEntryPoint
class BubbleActivity : ComponentActivity() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    @Inject
    lateinit var database: SuspiciousAppDao

    private var domain: String? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newDomain = intent?.getStringExtra(BUBBLE_DOMAIN_EXTRA_PARAM)
        Log.i(TAG, "old: $domain on new Intent: $newDomain")
        if (newDomain != null) {
            domain = newDomain
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            val newDomain = savedInstanceState.getString(BUBBLE_DOMAIN_EXTRA_PARAM)
            Log.i(TAG, "old: $domain on new saved instance: $newDomain")
            if (newDomain != null) {
                domain = newDomain
            }
        }

        setContent {
            ElaTheme {
                ChatScreen(
                    domain ?: "bubble",
                    emptyList(),
                    true,
                    onSubmit = {},
                    onReturn = null,
                )
            }
        }
    }

    companion object {
        const val BUBBLE_DOMAIN_EXTRA_PARAM = "domain"
    }
}
