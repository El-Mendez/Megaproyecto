package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.SupervisorJob
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.persistence.settings.ElaSettings
import javax.inject.Inject

private const val TAG = "ELA_NOTIFICATION_SERVICE"

class SuspiciousNotification : BroadcastReceiver() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>
    private val supervisor = SupervisorJob()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val domain = intent.getStringExtra("domain") ?: return
        val action = intent.getStringExtra("action") ?: return

        when (action) {
            "addToWhitelist" -> onAddToWhitelist(domain)
            "submitFromNotification" -> {
                val inputtedText = SuspiciousTrafficChannel.recoverSubmittedText(intent) ?: return
                SuspiciousTrafficChannel
                    .addMessageToChat(
                        context,
                        domain,
                        inputtedText,
                        true,
                    )
            }
//            "dismiss" ->
            else -> {
                Log.e(TAG, "unknown action: $action")
                return
            }
        }
    }

    private fun onAddToWhitelist(domain: String) {
//        CoroutineScope(Dispatchers.IO + supervisor).launch {
//            elaSettingsStore.updateData {
//                it.withAddedInWhitelist(domain)
//            }
//        }
    }

    companion object {
        fun submitFromNotification(context: Context, domain: String): Intent {
            return Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "submitFromNotification")
                putExtra("domain", domain)
            }
        }

        fun addToWhitelist(context: Context, domain: String): Intent {
            return Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "addToWhitelist")
                putExtra("domain", domain)
            }
        }

        fun submitFromChat(context: Context, domain: String, message: String): Intent {
            return Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "submitFromChat")
                putExtra("domain", domain)
                putExtra("message", message)
            }
        }

        fun dismissNotification(context: Context, domain: String): Intent {
            return Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "dismissNotification")
                putExtra("domain", domain)
            }
        }
    }
}
