package me.mendez.ela

import android.content.Intent
import android.content.pm.PackageManager
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            Log.i(TAG, "user accepted permissions")
            startVpn()
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

                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    Log.d(TAG, "starting vpn, no permissions needed")
                                    startVpn()
                                    return@SettingsScreen
                                }

                                if (permissionsAlreadyGranted()) {
                                    Log.d(TAG, "starting vpn, permissions already granted")
                                    startVpn()
                                } else {
                                    setVpnStatus(true)
                                    Log.i(TAG, "launching start vpn permissions requests")
                                    askForPermissions()
                                }
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
    private fun askForPermissions() {
        requestPermissionLauncher.launch(
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

    private fun startVpn() {
        setVpnStatus(true)
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
    private fun permissionsAlreadyGranted(): Boolean {
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


