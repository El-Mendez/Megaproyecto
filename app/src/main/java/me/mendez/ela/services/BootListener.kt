package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.mendez.ela.persistence.settings.ElaSettingsModule
import me.mendez.ela.vpn.ElaVpnService

private const val TAG = "ELA_BOOT"

class BootListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.e(TAG, "Intent or Context must not be null")
            return
        }

        if (Intent.ACTION_BOOT_COMPLETED != intent.action) {
            Log.i(TAG, "Not a boot. Ignoring intent.")
            return
        }

        val dataStore = ElaSettingsModule.provideElaSettingsStore(context)

        Log.i(TAG, "Boot event received")
        runBlocking {
            val state = dataStore.data.first()

            if (state.startOnBoot) {
                Log.i(TAG, "Start VPN because of settings")
                ElaVpnService.sendStart(context)
            } else if (state.vpnRunning || !state.ready) {
                Log.i(TAG, "Reset VPN status to off")
                ElaVpnService.showRunning(dataStore, running = false, ready = true)
            }
        }


    }
}
