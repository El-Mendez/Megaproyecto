package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule
import me.mendez.ela.vpn.ElaVpn

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
                ElaVpn.sendStart(context)
            } else if (state.vpnRunning) {
                Log.d(TAG, "resetting running state to false")
                ElaVpn.showRunning(false, dataStore)
            }
        }


    }
}