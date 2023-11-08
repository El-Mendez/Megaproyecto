package me.mendez.ela.services

import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.ml.MaliciousDomainClassifier

private const val TAG = "ELA_FAKE_TRAFFIC"

class FakeTraffic : TileService() {
    override fun onClick() {
        val domains = listOf("ejemplo.com", "ejemplo2.com", "ejemplo3.com")

        Log.i(TAG, "Starting fake notification")
        val domain = domains.random()

        Log.d(TAG, "3")
        Thread.sleep(1000)
        Log.d(TAG, "2")
        Thread.sleep(1000)
        Log.d(TAG, "1")
        Thread.sleep(1000)

        SuspiciousNotification.createChat(
            this,
            domain,
            -1,
            MaliciousDomainClassifier.Result.BENIGN
        )

        super.onClick()
    }
}
