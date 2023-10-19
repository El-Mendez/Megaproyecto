package me.mendez.ela

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newDomain = intent?.getStringExtra(BUBBLE_DOMAIN_EXTRA_PARAM)
        if (newDomain != null) domain = newDomain
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var newDomain = intent.getStringExtra(BUBBLE_DOMAIN_EXTRA_PARAM)
        if (newDomain != null) domain = newDomain

        newDomain = savedInstanceState?.getString(BUBBLE_DOMAIN_EXTRA_PARAM)
        if (newDomain != null) domain = newDomain

        if (domain == null) {
            finish()
            return
        }

        val viewModel: BubbleViewModel by viewModels {
            BubbleViewModel.provideBubbleFactory(factory, domain!!)
        }

        setContent {
            ElaTheme {
                ChatScreen(
                    viewModel.domain,
                    viewModel.messages,
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
        const val BUBBLE_DOMAIN_EXTRA_PARAM = "domain"

        fun createLaunchIntent(context: Context, domain: String): PendingIntent {
            return PendingIntent.getActivity(
                context,
                ":openBubbleActivity:$domain:".hashCode(),
                Intent(context, BubbleActivity::class.java).apply {
                    setAction(Intent.ACTION_VIEW)
                    putExtra(BUBBLE_DOMAIN_EXTRA_PARAM, domain)
                },
                PendingIntent.FLAG_MUTABLE,
            )
        }
    }
}
