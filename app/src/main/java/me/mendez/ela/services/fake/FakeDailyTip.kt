package me.mendez.ela.services.fake

import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.services.DailyTip

private const val TAG = "ELA_FAKE_DAILY_TIP"

class FakeDailyTip : TileService() {
    override fun onClick() {
        Log.i(TAG, "Starting fake daily tip")
        sendBroadcast(
            Intent(this, DailyTip::class.java),
        )
        super.onClick()
    }
}
