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

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            onBoot(context)
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            onUpdate(context)
        } else {
            Log.i(TAG, "Not a boot. Ignoring intent.")
        }
    }

    private fun onUpdate(context: Context) {
        val dataStore = ElaSettingsModule.provideElaSettingsStore(context)
        Log.d(TAG, "app updated!")
        val state = runBlocking {
            dataStore.data.first()
        }

        if (state.vpnRunning) {
            Log.i(TAG, "restarting vpn after app update")
            ElaVpnService.sendStart(context)
        } else {
            Log.i(TAG, "App updated, nothing to do")
        }
    }

    private fun onBoot(context: Context) {
        val dataStore = ElaSettingsModule.provideElaSettingsStore(context)

        Log.i(TAG, "Running phone booted code")
        runBlocking {
            val state = dataStore.data.first()

            if (state.startOnBoot) {
                Log.d(TAG, "Start VPN because of settings")
                ElaVpnService.sendStart(context)
            } else if (state.vpnRunning || !state.ready) {
                Log.d(TAG, "Reset VPN status to off")
                ElaVpnService.showRunning(dataStore, running = false, ready = true)
            }
        }
    }
}
