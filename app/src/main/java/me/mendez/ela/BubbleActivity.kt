package me.mendez.ela

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.screens.chat.BubbleViewModel
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

    @Inject
    lateinit var factory: BubbleViewModel.Factory

    private var domain: String? = null
    private var conversation: Long? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "on new intent: ${intent?.extras}")
        updateParams(intent, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateParams(intent, savedInstanceState)

        if (domain == null || conversation == null) {
            Log.e(TAG, "domain or conversation was null. domain: $domain, conversation: $conversation")
            finish()
            return
        }

        val viewModel: BubbleViewModel by viewModels {
            BubbleViewModel.provideBubbleFactory(factory, domain!!, conversation!!)
        }

        setContent {
            val messages = viewModel.messages.collectAsState(emptyList())

            ElaTheme {
                ChatScreen(
                    viewModel.domain,
                    messages.value,
                    viewModel.calculatingResponse.value,
                    onSubmit = viewModel::sendMessage,
                    onReturn = null,
                    showAddToWhitelist = !viewModel.inWhitelist.collectAsState(false).value &&
                            !viewModel.ignoreAddToWhitelist.value,
                    onWhitelistAccept = viewModel::addToWhitelist,
                )
            }
        }
    }

    companion object {
        private const val BUBBLE_DOMAIN_EXTRA_PARAM = "domain"
        private const val BUBBLE_CONVERSATION_PARAM = "conversation"

        fun createLaunchIntent(context: Context, domain: String, conversation: Long): PendingIntent {
            return PendingIntent.getActivity(
                context,
                ":openBubbleActivity:$domain:".hashCode(),
                Intent(context, BubbleActivity::class.java).apply {
                    setAction(Intent.ACTION_VIEW)
                    putExtra(BUBBLE_DOMAIN_EXTRA_PARAM, domain)
                    putExtra(BUBBLE_CONVERSATION_PARAM, conversation.toString())
                },
                PendingIntent.FLAG_MUTABLE,
            )
        }
    }

    private fun updateParams(intent: Intent?, savedInstanceState: Bundle?) {
        var newConversation: Long? = null
        var newDomain: String? = null

        val intentString = if (intent == null) {
            "null"
        } else {
            intent.extras?.keySet()?.joinToString(", ", "{", "}") { key ->
                "$key=${intent.extras?.get(key).toString()}"
            }

        }
        Log.d(TAG, "intent: $intentString \nbundle: $savedInstanceState")
        if (intent != null) {
            newDomain = intent.getStringExtra(BUBBLE_DOMAIN_EXTRA_PARAM)
            newConversation = intent.getStringExtra(BUBBLE_CONVERSATION_PARAM)?.toLongOrNull()
        }

        if ((newDomain == null || newConversation == -2L) && savedInstanceState != null) {
            newDomain = savedInstanceState.getString(BUBBLE_DOMAIN_EXTRA_PARAM)
            newConversation = savedInstanceState.getString(BUBBLE_CONVERSATION_PARAM)?.toLongOrNull()
        }

        if (newDomain != null && newConversation != null && newConversation != -2L) {
            domain = newDomain
            conversation = newConversation
        }
    }
}
