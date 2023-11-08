package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.ml.MaliciousDomainClassifier

private const val TAG = "ELA_NOTIFICATION_SERVICE"


@AndroidEntryPoint
class SuspiciousNotification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val domain = intent.getStringExtra("domain") ?: return
        val actionString = intent.getStringExtra("action") ?: return
        val conversation = intent.getStringExtra("conversation")?.toLongOrNull() ?: return

        Log.d(TAG, "new action: $actionString. domain: $domain. conversation: $conversation")

        if (actionString == "CREATE_CHAT") {
            initializeChat(domain, context)
            return
        }
    }

    private fun initializeChat(domain: String, context: Context) {
        SuspiciousTrafficChannel.notify(context, domain.hashCode()) {
            newSuspiciousTraffic(domain)
        }
    }

    companion object {
        fun createChat(context: Context, domain: String, conversation: Long, reason: MaliciousDomainClassifier.Result) {
            val intent = Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "CREATE_CHAT")
                putExtra("domain", domain)
                putExtra("reason", reason.toString())
                putExtra("conversation", conversation.toString())
            }

            context.sendBroadcast(intent)
        }
    }
}
