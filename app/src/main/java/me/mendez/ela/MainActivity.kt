package me.mendez.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.services.PermissionCheck
import me.mendez.ela.ui.screens.DailyBlocksScreen
import me.mendez.ela.ui.screens.MainScreen
import me.mendez.ela.ui.screens.suspicious.SuspiciousAppsScreen
import me.mendez.ela.ui.screens.chat.ChatScreen
import me.mendez.ela.ui.screens.chat.ChatViewModel
import me.mendez.ela.ui.screens.settings.SettingsNestedGraph
import me.mendez.ela.ui.screens.settings.SettingsViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    @Inject
    lateinit var suspiciousApps: SuspiciousAppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionCheck.notify(this)

        setContent {
            ElaTheme {
                val navController = rememberNavController()
                val chatViewModel = viewModel<ChatViewModel>()
                val elaSettings = elaSettingsStore.data.collectAsState(initial = ElaSettings.default()).value
                val suspiciousApps = suspiciousApps.getAll().collectAsState(initial = emptyList())

                val settingsViewModel = viewModel<SettingsViewModel>()
                val startActivityForResultContract = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                    onResult = { settingsViewModel.onVpnPermissionResponse(it, this@MainActivity) }
                )
                val askPermissionContract = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        settingsViewModel.onNotificationPermissionResponse(
                            it,
                            this@MainActivity,
                            startActivityForResultContract
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
                            },
                            suspiciousAppsAmount = suspiciousApps.value.size,
                            onSuspiciousAppClick = {
                                navController.navigate("app-details")
                            },
                            blocks = 100,
                        )
                    }

                    composable("chat") {
                        ChatScreen(
                            "Ela",
                            chatViewModel.messages,
                            chatViewModel.calculatingResponse.value,
                            onSubmit = chatViewModel::sendMessage,
                            onReturn = navController::popBackStack
                        )
                    }

                    composable("settings") {
                        SettingsNestedGraph(
                            onReturn = navController::popBackStack,
                            settings = settingsViewModel.state.collectAsState(initial = ElaSettings.default()).value,
                            update = {
                                settingsViewModel.updateSettings(
                                    it,
                                    this@MainActivity,
                                    askPermissionContract,
                                    startActivityForResultContract
                                )
                            }
                        )
                    }

                    composable("details") {
                        DailyBlocksScreen(
                            onReturn = navController::popBackStack
                        )
                    }

                    composable("app-details") {
                        SuspiciousAppsScreen(
                            remember { derivedStateOf { suspiciousApps.value.map { it.packageName } } }.value,
                            navController::popBackStack
                        )
                    }
                }
            }
        }
    }
}
