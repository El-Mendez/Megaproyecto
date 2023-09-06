package me.mendez.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.services.NotificationService
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.ui.screens.DailyBlocksScreen
import me.mendez.ela.ui.screens.MainScreen
import me.mendez.ela.ui.screens.chat.ChatScreen
import me.mendez.ela.ui.screens.chat.ChatViewModel
import me.mendez.ela.ui.screens.settings.SettingsScreen
import me.mendez.ela.ui.screens.settings.SettingsViewModel
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

                val settingsViewModel = viewModel<SettingsViewModel>()
                val intentContract = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                    onResult = { settingsViewModel.onVpnPermissionResponse(it, this@MainActivity) }
                )
                val stringContract = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        settingsViewModel.onNotificationPermissionResponse(
                            it,
                            this@MainActivity,
                            intentContract
                        )
                    })

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
                            onReturn = navController::popBackStack

                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onReturn = navController::popBackStack,
                            settingsStore = elaSettingsStore,
                            startVpn = {
                                settingsViewModel.trySetVpnStatus(it, this@MainActivity, stringContract, intentContract)
                            },
                            onUpdate = { settingsViewModel.restartVpn(this@MainActivity) }
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


