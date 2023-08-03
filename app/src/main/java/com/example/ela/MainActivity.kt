package com.example.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ela.services.NotificationService
import com.example.ela.settings.ElaSettings
import com.example.ela.ui.screens.*
import com.example.ela.ui.theme.ElaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
                val scope = rememberCoroutineScope()
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
                            isEnabled = elaSettings.blockDefault,
                            onToggle = {
                                scope.launch {
                                    elaSettingsStore.updateData { old ->
                                        old.copy(blockDefault = it)
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


