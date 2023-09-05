package me.mendez.ela

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.services.NotificationService
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.mendez.ela.ui.screens.*
import me.mendez.ela.vpn.ElaVpn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService
    private val TAG = "MAIN"

    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    private val waitForIntentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, "vpn permission state $it")
        if (it.resultCode == Activity.RESULT_OK) {
            startVpn()
        } else {
            setVpnStatus(false)
        }
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            Log.i(TAG, "user notification permission accepted")
            tryStartVpn()
        } else {
            Log.i(TAG, "user rejected permissions. Turn off vpn")
            setVpnStatus(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationService = NotificationService(applicationContext)

        setContent {
            ElaTheme {
                val navController = rememberNavController()
                val chatViewModel = viewModel<ChatViewModel>()
                val elaSettings = elaSettingsStore.data.collectAsState(initial = ElaSettings.default()).value

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        MainScreen(
                            onSend = {
                                navController.navigate("chat")
                            },
                            onSettings = {
                                navController.navigate("settings")
                            },
                            onDetails = {
                                navController.navigate("details")
                            },
                            vpnEnabled = elaSettings.vpnRunning,
                            enableVpn = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("chat") {
                        ChatScreen(
                            chatViewModel.messages,
                            chatViewModel.calculatingResponse.value,
                            onSubmit = chatViewModel::sendMessage,
                            onReturn = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onReturn = { navController.popBackStack() },
                            settingsStore = elaSettingsStore,
                            startVpn = {
                                if (!it) {
                                    Log.d(TAG, "stopping vpn")
                                    stopVpn()
                                    return@SettingsScreen
                                }

                                setVpnStatus(true)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (!notificationPermission()) {
                                        Log.i(TAG, "notifications permissions not granted!")
                                        askForNotificationPermissions()
                                        return@SettingsScreen
                                    }
                                }

                                Log.d(TAG, "notification permissions is already granted")
                                tryStartVpn()
                            },
                            onUpdate = { restartVpn() }
                        )
                    }

                    composable("details") {
                        DailyBlocksScreen {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForNotificationPermissions() {
        requestPermissions.launch(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private fun setVpnStatus(on: Boolean) {
        runBlocking {
            withContext(Dispatchers.IO) {
                elaSettingsStore.updateData { old ->
                    old.copy(vpnRunning = on)
                }
            }
        }
    }

    private fun tryStartVpn() {
        setVpnStatus(true)

        val status = VpnService.prepare(this)
        if (status != null) {
            Log.d(TAG, "waiting for service $status")
            waitForIntentResult.launch(status)
            return
        }

        startVpn()
    }

    private fun startVpn() {
        Intent(applicationContext, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.START.toString()
            startService(intent)
        }
    }

    private fun stopVpn() {
        setVpnStatus(false)
        Intent(applicationContext, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.STOP.toString()
            startService(intent)
        }
    }

    private fun restartVpn() {
        setVpnStatus(true)
        Intent(applicationContext, ElaVpn::class.java).also { intent ->
            intent.action = ElaVpn.Commands.RESTART.toString()
            startService(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStop() {
        super.onStop()
        notificationService.showNotification(5, "WhatsApp")
    }
}


