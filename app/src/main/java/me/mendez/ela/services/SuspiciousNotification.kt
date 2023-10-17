package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "ELA_NOTIFICATION_SERVICE"

class SuspiciousNotification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val domain = intent.getStringExtra("domain") ?: return
        val action = intent.getStringExtra("action") ?: return

        when (action) {
            "whitelist" -> println("trying to whitelist")
            "reply" -> println("reply")
//            "dismiss" ->
            else -> {
                Log.e(TAG, "unknown action: $action")
                return
            }
        }
    }
}
