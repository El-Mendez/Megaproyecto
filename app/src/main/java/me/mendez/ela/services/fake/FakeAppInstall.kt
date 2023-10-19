package me.mendez.ela.services.fake

import android.service.quicksettings.TileService
import android.util.Log
import me.mendez.ela.services.PermissionCheck

private const val TAG = "ELA_FAKE_APP_INSTALL"

class FakeAppInstall : TileService() {
    override fun onClick() {
        Log.i(TAG, "Starting fake app install")
        PermissionCheck.notify(this)
        super.onClick()
    }
}
