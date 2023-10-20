package me.mendez.ela.services.fake

import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.ml.MaliciousDomainClassifier
import me.mendez.ela.services.SuspiciousNotification

private const val TAG = "ELA_FAKE_TRAFFIC"

class FakeTraffic : TileService() {
    override fun onClick() {
        Log.i(TAG, "Starting fake notification")
        val domain = "ejemplo.com"
        Thread.sleep(3000)

        SuspiciousNotification.createChat(
            this,
            domain,
            MaliciousDomainClassifier.Result.BENIGN
        )

        Log.i(TAG, "End fake notification")
        super.onClick()
    }
}
