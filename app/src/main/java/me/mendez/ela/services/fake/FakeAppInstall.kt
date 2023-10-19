package me.mendez.ela.services.fake

import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.services.PermissionCheck

private const val TAG = "ELA_FAKE_APP_INSTALL"

class FakeAppInstall : TileService() {
    override fun onClick() {
        Log.i(TAG, "Starting fake app install")

        val intent = Intent(this, PermissionCheck::class.java)
        intent.action = "me.mendez.ela.TEST_APP_INSTALL"
        sendBroadcast(intent)
        super.onClick()
    }
}
