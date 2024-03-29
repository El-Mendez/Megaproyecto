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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.mendez.ela.persistence.database.BLOCK_DATABASE_NAME
import me.mendez.ela.persistence.database.DatabaseProvider
import me.mendez.ela.persistence.settings.ActionNeeded
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.persistence.settings.nextAction
import me.mendez.ela.vpn.ElaVpnService
import javax.inject.Inject

private const val TAG = "ELA_SETTINGS"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<ElaSettings>,
) : ViewModel() {
    val state: Flow<ElaSettings>
        get() = dataStore.data

    fun shareBlockDatabase(context: Context) {
        val databaseUri = DatabaseProvider()
            .getUri(context, BLOCK_DATABASE_NAME)

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            setType("application/vnd.sqlite3")
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(databaseUri.toList()))
        }
        context.startActivity(intent)
    }

    fun updateSettings(
        updater: (ElaSettings) -> ElaSettings,
        context: Context,
        askPermissionContract: ActivityResultLauncher<String>,
        startActivityForResultContract: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            if (!state.first().ready) return@launch

            when (dataStore.nextAction(updater)) {
                ActionNeeded.START -> {
                    Log.i(TAG, "sending start vpn request")
                    tryActivateVpn(context, askPermissionContract, startActivityForResultContract)
                }

                ActionNeeded.STOP -> {
                    Log.i(TAG, "sending stop vpn request")
                    ElaVpnService.sendStop(context)
                }

                ActionNeeded.RESTART -> {
                    Log.i(TAG, "changes were made when it was on. Send restart vpn request")
                    ElaVpnService.sendRestart(context)
                }

                ActionNeeded.NONE -> {
                    Log.d(TAG, "Changes were made but it was off. Nothing to do.")
                }
            }
        }
    }

    private fun tryActivateVpn(
        context: Context,
        askPermissionContract: ActivityResultLauncher<String>,
        startActivityForResultContract: ActivityResultLauncher<Intent>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationPermission(context) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "We don't have notifications permissions yet! Waiting for user to accept them.")
                askForNotificationPermissions(askPermissionContract)
                return
            }
        }

        onNotificationPermissionResponse(true, context, startActivityForResultContract)
    }

    fun onNotificationPermissionResponse(
        allowed: Boolean,
        context: Context,
        startActivityForResultContract: ActivityResultLauncher<Intent>
    ) {
        if (!allowed) {
            Log.i(TAG, "User rejected notification permissions. Cancel trying to turn on VPN")
            cancelVpnStartRequest(dataStore)
            return
        }

        Log.d(TAG, "User notification permission accepted")

        val status = VpnService.prepare(context)
        if (status != null) {
            Log.i(TAG, "User hasn't approved the VPN use yet. Wait for their confirmation.")
            askForVpnPermission(startActivityForResultContract, status)
            return
        }

        onVpnPermissionResponse(
            ActivityResult(Activity.RESULT_OK, null),
            context,
        )
    }

    fun onVpnPermissionResponse(result: ActivityResult, context: Context) {
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "vpn permissions granted")
            Log.i(TAG, "Vpn is ready to start!")
            ElaVpnService.sendStart(context)
        } else {
            Log.i(TAG, "vpn permissions denied")
            cancelVpnStartRequest(dataStore)
        }
    }

    private fun cancelVpnStartRequest(dataStore: DataStore<ElaSettings>) {
        viewModelScope.launch {
            ElaVpnService.showRunning(dataStore, running = false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationPermission(context: Context): Int {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForNotificationPermissions(stringContract: ActivityResultLauncher<String>) {
        stringContract.launch(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private fun askForVpnPermission(activityContract: ActivityResultLauncher<Intent>, intent: Intent) {
        activityContract.launch(intent)
    }
}
