package me.mendez.ela.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.vpn.ElaVpn
import javax.inject.Inject

private const val TAG = "ELA_TILE"

@AndroidEntryPoint
class QuickSetting : TileService() {
    @Inject
    lateinit var dataStore: DataStore<ElaSettings>
    private val supervisor = SupervisorJob()
    private var state = ElaSettings.default()

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "tile created")
        CoroutineScope(Dispatchers.IO + supervisor).launch {
            dataStore.data
                .onEach {
                    Log.i(TAG, "updated status ${it.vpnRunning}")
                    state = it
                    qsTile?.state = if (it.vpnRunning)
                        Tile.STATE_ACTIVE
                    else
                        Tile.STATE_INACTIVE
                    qsTile?.updateTile()
                }.collect()
        }
    }

    override fun onClick() {
        super.onClick()

        if (state.vpnRunning) {
            ElaVpn.sendStop(this)
            return
        }

        if (!ElaVpn.haveAllPermissions(this)) {
            Log.i(TAG, "not enough permissions to start vpn")
            return
        }

        ElaVpn.sendStart(this)
    }

    override fun onDestroy() {
        supervisor.cancel()
        super.onDestroy()
    }
}