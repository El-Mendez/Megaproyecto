package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.persistence.settings.ElaSettingsModule
import me.mendez.ela.vpn.ElaVpnService

private const val TAG = "ELA_BOOT"

class BootListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.e(TAG, "something was null. abort booting")
            return
        }

        if (Intent.ACTION_BOOT_COMPLETED != intent.action) {
            Log.d(TAG, "not a boot. Ignoring intent.")
            return
        }

        val dataStore = ElaSettingsModule.provideElaSettingsStore(context)

        Log.d(TAG, "boot event received")
        runBlocking {
            lateinit var state: ElaSettings
            dataStore.data.collect { state = it }

            if (state.startOnBoot) {
                Log.i(TAG, "starting vpn")
                ElaVpnService.sendStart(context)
            } else if (state.vpnRunning) {
                Log.d(TAG, "resetting running state to false")
                ElaVpnService.showRunning(false, dataStore)
            }
        }


    }
}
