package me.mendez.ela.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking
import me.mendez.ela.chat.ChatApi
import me.mendez.ela.chat.Message
import me.mendez.ela.notifications.DailyTipChannel
import java.util.*
import javax.inject.Inject

private const val TAG = "ELA_DAILY_TIP"

class DailyTip : BroadcastReceiver() {
    @Inject
    lateinit var chatApi: ChatApi
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.i(TAG, "creating new daily tip")
        val response = runBlocking {
            try {
                chatApi.answer(
                    listOf(Message("dame un dato interesante de ciberseguridad", true, Date()))
                )
            } catch (e: Exception) {
                return@runBlocking emptyList()
            }
        }

        if (response.isEmpty()) return

        DailyTipChannel.notify(
            context,
            DailyTipChannel.TIP_ID,
        ) {
            newDailyTip(response.first().content)
        }
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 1

        fun scheduleNotifications(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)

            val nextMidday = Calendar.getInstance().apply {
                if (get(Calendar.HOUR_OF_DAY) >= 12) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }

                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }


            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextMidday.timeInMillis - System.currentTimeMillis(),
                AlarmManager.INTERVAL_DAY,
                PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    Intent(context, DailyTip::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
    }
}
