package me.mendez.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import me.mendez.ela.persistence.database.apps.SuspiciousApp
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.database.blocks.BlockDao
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
    lateinit var suspiciousAppsDao: SuspiciousAppDao

    @Inject
    lateinit var blocks: BlockDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionCheck.notify(this)

        setContent {
            ElaTheme {
                val navController = rememberNavController()
                val chatViewModel = viewModel<ChatViewModel>()

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
                        val suspiciousApps by suspiciousAppsDao.getAmount()
                            .collectAsState(0)

                        val totalBlocks by blocks.amount()
                            .collectAsState(0)

                        val topDailyBlocks by blocks.topDailyBlocks()
                            .collectAsState(emptyList())

                        val dailyBlockAmount by blocks.dailyBlockAmount()
                            .collectAsState(0)

                        val elaSettings by elaSettingsStore.data.collectAsState(
                            initial = ElaSettings(
                                vpnRunning = true,
                                startOnBoot = false,
                                blockDefault = true,
                                ready = true,
                                emptyList(),
                            )
                        )

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
                            onSuspiciousAppClick = {
                                navController.navigate("app-details")
                            },
                            suspiciousAppsAmount = suspiciousApps,
                            totalBlocks = totalBlocks,
                            dailyBlocks = topDailyBlocks,
                            totalDailyBlocks = dailyBlockAmount,
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
                        val settings by settingsViewModel.state
                            .collectAsState(initial = ElaSettings.default())

                        SettingsNestedGraph(
                            onReturn = navController::popBackStack,
                            settings = settings,
                            update = {
                                settingsViewModel.updateSettings(
                                    it,
                                    this@MainActivity,
                                    askPermissionContract,
                                    startActivityForResultContract
                                )
                            },
                            onExport = {
                                settingsViewModel.shareBlockDatabase(this@MainActivity)
                            }
                        )
                    }

                    composable("details") {
                        val dailyBlocks by blocks.dailyBlocks()
                            .collectAsState(emptyList())

                        DailyBlocksScreen(
                            onReturn = navController::popBackStack,
                            dailyBlocks = dailyBlocks,
                        )
                    }

                    composable("app-details") {
                        val suspiciousApps: List<SuspiciousApp>? by suspiciousAppsDao.getAll()
                            .collectAsState(null)

                        SuspiciousAppsScreen(
                            suspiciousApps?.map { it.packageName },
                            navController::popBackStack
                        )
                    }
                }
            }
        }
    }
}
