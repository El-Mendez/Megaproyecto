package me.mendez.ela.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import me.mendez.ela.chat.ChatApi
import me.mendez.ela.notifications.DailyTipChannel
import java.util.*
import javax.inject.Inject

private const val TAG = "ELA_DAILY_TIP"

@AndroidEntryPoint
class DailyTip : BroadcastReceiver() {
    @Inject
    lateinit var chatApi: ChatApi
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.i(TAG, "creating new daily tip")
        val response = runBlocking { chatApi.dailyTip() }

        if (response.isNullOrEmpty()) {
            Log.i(TAG, "could not get ela chat api response")
            return
        }

        DailyTipChannel.notify(
            context,
            DailyTipChannel.TIP_ID,
        ) {
            newDailyTip(response.map { it.content })
        }
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 10

        fun scheduleNotifications(context: Context) {
            if (PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    Intent(context, DailyTip::class.java),
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_NO_CREATE,
                ) != null
            ) {
                Log.i(TAG, "alarm already scheduled, no need to recreate it")
                return
            }

            val alarmManager = context.getSystemService(AlarmManager::class.java)

            val nextMidday = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            Log.i(
                TAG,
                "scheduling alarm at ${(nextMidday.timeInMillis - System.currentTimeMillis()) / (1000 * 60)} minutes"
            )

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextMidday.timeInMillis - System.currentTimeMillis(),
                AlarmManager.INTERVAL_HALF_DAY,
                PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    Intent(context, DailyTip::class.java),
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
    }
}
