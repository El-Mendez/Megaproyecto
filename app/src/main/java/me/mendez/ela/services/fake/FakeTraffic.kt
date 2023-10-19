package me.mendez.ela.services.fake

import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.notifications.SuspiciousTrafficChannel

private const val TAG = "ELA_FAKE_TRAFFIC"

class FakeTraffic : TileService() {
    override fun onClick() {
        Log.i(TAG, "Starting fake notification")
        val domain = "ejemplo.com"
        Thread.sleep(3000)
        SuspiciousTrafficChannel.notify(this, domain.hashCode()) {
            newSuspiciousTraffic(domain, "FROM FAKE TRAFFIC")
        }
        Log.i(TAG, "End fake notification")
        super.onClick()
    }
}
