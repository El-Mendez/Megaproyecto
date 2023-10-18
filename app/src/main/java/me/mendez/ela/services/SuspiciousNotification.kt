package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import me.mendez.ela.notifications.SuspiciousTrafficChannel

private const val TAG = "ELA_NOTIFICATION_SERVICE"

class SuspiciousNotification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val domain = intent.getStringExtra("domain") ?: return
        val action = intent.getStringExtra("action") ?: return

        when (action) {
            "addToWhitelist" -> println("trying to whitelist")
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
