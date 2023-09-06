package me.mendez.ela.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule
import me.mendez.ela.vpn.ElaVpn

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

        if (state.vpnRunning) {
            CoroutineScope(Dispatchers.IO + supervisor).launch {
                Intent(this@QuickSetting, ElaVpn::class.java).also { intent ->
                    intent.action = ElaVpn.Commands.STOP.toString()
                    startService(intent)
                }

                dataStore.updateData { old ->
                    old.copy(vpnRunning = false)
                }
            }
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && VpnService.prepare(this) != null
        ) {
            Log.i(TAG, "not enough permissions")
            return
        }


        CoroutineScope(Dispatchers.IO + supervisor).launch {
            dataStore.updateData {
                it.copy(vpnRunning = true)
            }
        }

        Intent(this@QuickSetting, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.START.toString()
            startService(intent)
        }
    }

    override fun onDestroy() {
        supervisor.cancel()
        super.onDestroy()
    }
}