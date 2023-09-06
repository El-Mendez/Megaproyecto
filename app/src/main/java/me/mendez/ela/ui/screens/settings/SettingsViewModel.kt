package me.mendez.ela.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.vpn.ElaVpn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<ElaSettings>,
) : ViewModel() {
    private val TAG = "SETTINGS"

    val state: Flow<ElaSettings>
        get() = dataStore.data

    fun updateSettings(
        updater: (ElaSettings) -> ElaSettings,
        context: Context,
        stringContract: ActivityResultLauncher<String>,
        intentContract: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.updateData { old ->
                    val updated = updater(old)
                    if (old.vpnRunning != updated.vpnRunning) {
                        trySetVpnStatus(updated.vpnRunning, context, stringContract, intentContract)
                    } else if (old.vpnRunning && old != updated) {
                        restartVpn(context)
                    }
                    updated
                }
            }
        }
    }

    private fun trySetVpnStatus(
        it: Boolean,
        context: Context,
        stringContract: ActivityResultLauncher<String>,
        intentContract: ActivityResultLauncher<Intent>
    ) {
        if (!it) {
            Log.d(TAG, "stopping vpn")
            stopVpn(context)
            return
        }

        setVpnStatus(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationPermission(context)) {
                Log.i(TAG, "notification permissions not granted!")
                askForNotificationPermissions(stringContract)
                return
            }
        }

        Log.d(TAG, "notification permissions are already granted")
        tryStartVpn(context, intentContract)
    }

    private fun setVpnStatus(on: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.updateData { old ->
                    old.copy(vpnRunning = on)
                }
            }
        }
    }

    private fun tryStartVpn(context: Context, intentContract: ActivityResultLauncher<Intent>) {
        setVpnStatus(true)

        val status = VpnService.prepare(context)
        if (status != null) {
            Log.d(TAG, "waiting for service $status")
            askForVpnPermission(intentContract, status)
            return
        }
        startVpn(context)
    }


    private fun startVpn(context: Context) {
        setVpnStatus(true)
        Intent(context, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.START.toString()
            context.startService(intent)
        }
    }

    private fun stopVpn(context: Context) {
        setVpnStatus(false)
        Intent(context, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.STOP.toString()
            context.startService(intent)
        }
    }

    private fun restartVpn(context: Context) {
        setVpnStatus(true)
        Intent(context, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.RESTART.toString()
            context.startService(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForNotificationPermissions(stringContract: ActivityResultLauncher<String>) {
        stringContract.launch(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    }

    fun onNotificationPermissionResponse(
        allowed: Boolean,
        context: Context,
        intentContract: ActivityResultLauncher<Intent>
    ) {
        if (allowed) {
            Log.i(TAG, "user notification permission accepted")
            tryStartVpn(context, intentContract)
        } else {
            Log.i(TAG, "user rejected permissions. Turn off vpn")
            setVpnStatus(false)
        }
    }

    private fun askForVpnPermission(activityContract: ActivityResultLauncher<Intent>, intent: Intent) {
        activityContract.launch(intent)
    }

    fun onVpnPermissionResponse(result: ActivityResult, context: Context) {
        Log.d(TAG, "vpn permission state $result")
        if (result.resultCode == Activity.RESULT_OK) {
            startVpn(context)
        } else {
            setVpnStatus(false)
        }
    }
}