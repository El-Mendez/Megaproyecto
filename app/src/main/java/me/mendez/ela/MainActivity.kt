package me.mendez.ela

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.services.NotificationService
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.ui.screens.*
import me.mendez.ela.vpn.ElaVpn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService

    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

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
                            requestVpnPermission = {
                                ElaVpn.createChannel(this@MainActivity)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    ActivityCompat.requestPermissions(
                                        this@MainActivity,
                                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                        0
                                    )
                                }

                                Intent(applicationContext, ElaVpn::class.java).also { intent ->
                                    intent.action =
                                        if (it) {
                                            ElaVpn.Commands.START.toString()
                                        } else {
                                            ElaVpn.Commands.STOP.toString()
                                        }
                                    startService(intent)
                                }
                            },
                            onUpdate = {
                                Intent(applicationContext, ElaVpn::class.java).also { intent ->
                                    if (elaSettings.vpnRunning) {
                                        intent.action = ElaVpn.Commands.RESTART.toString()
                                        startService(intent)
                                    }
                                }
                            }
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

    override fun onStop() {
        super.onStop()
        notificationService.showNotification(5, "WhatsApp")
    }
}


