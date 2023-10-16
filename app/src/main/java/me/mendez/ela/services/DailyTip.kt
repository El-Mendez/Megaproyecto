package me.mendez.ela.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.mendez.ela.notifications.DailyTipChannel
import java.util.*

class DailyTip : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        DailyTipChannel.notify(
            context,
            DailyTipChannel.TIP_ID,
            DailyTipChannel.newDailyTip(context, "¡hola mundo!")
        )
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 1

        fun scheduleNotifications(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val intent = Intent(context, DailyTip::class.java)

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
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getBroadcast(
                    context,
                    ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
    }
}
