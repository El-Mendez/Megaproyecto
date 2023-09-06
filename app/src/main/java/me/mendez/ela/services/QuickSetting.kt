package me.mendez.ela.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule

class QuickSetting : TileService() {
    private val TAG = "TILE"
    private val dataStore = ElaSettingsModule.provideElaSettingsStore(this)
    private val supervisor = SupervisorJob()
    private var state = ElaSettings.default()

    override fun onCreate() {
        Log.d(TAG, "tile created")
        super.onCreate()
        CoroutineScope(Dispatchers.IO + supervisor).launch {
            qsTile?.state = Tile.STATE_INACTIVE
            dataStore.data
                .onEach {
                    Log.i(TAG, "updated status ${it.vpnRunning}")
                    state = it
                    qsTile?.state = if (it.vpnRunning)
                        Tile.STATE_ACTIVE
                    else
                        Tile.STATE_INACTIVE
                    qsTile.updateTile()
                }.collect()
        }
    }

    override fun onClick() {
        super.onClick()

//        if (state.vpnRunning) {
//            CoroutineScope(Dispatchers.IO + supervisor).launch {
//
//            }
//            return
//        }
//
//        Log.i(TAG, "clicked!")
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.i("not enough permissions to activate")
//            return
//        }
//        qsTile.state = Tile.STATE_ACTIVE
    }

    override fun onDestroy() {
        supervisor.cancel()
        super.onDestroy()
    }
}