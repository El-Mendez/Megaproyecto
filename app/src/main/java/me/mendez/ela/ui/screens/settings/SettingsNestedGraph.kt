package me.mendez.ela.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mendez.ela.persistence.settings.ElaSettings

@Composable
fun SettingsNestedGraph(
    onReturn: (() -> Unit)?,
    settings: ElaSettings,
    update: ((ElaSettings) -> ElaSettings) -> Unit,
    onExport: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "general") {
        composable("general") {
            SettingsScreen(
                onReturn,
                settings,
                update,
                onAddDomains = {
                    navController.navigate("whitelist")
                },
                onExport = onExport,
            )
        }

        composable("whitelist") {
            AddDomainsScreen(
                onReturn = navController::popBackStack,
                settings,
                update,
            )
        }
    }
}
