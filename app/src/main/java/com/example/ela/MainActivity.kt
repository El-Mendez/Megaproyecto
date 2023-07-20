package com.example.ela

import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ela.model.AppBlock
import com.example.ela.remote.ChatApi
import com.example.ela.services.NotificationService
import com.example.ela.ui.theme.ElaTheme

class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService
    private val chatApi = ChatApi.create()
    override fun onCreate(savedInstanceState: Bundle?) {
//        val apps = applicationContext.packageManager.getInstalledApplications(0)
//        val appBlocks = mutableListOf<AppBlock>()
//
//        apps.forEach {
//            val icon = it.loadIcon(applicationContext.packageManager)
//            val name = it.loadLabel(applicationContext.packageManager).toString()
//            val isSystemApp = (it.flags.and(ApplicationInfo.FLAG_SYSTEM)) != 0
//            val isSystemApp = false

//            if (!isSystemApp && !name.isNullOrEmpty() && icon != null) {
//                appBlocks.add(AppBlock(name, listOf(), icon))
//            }
//        }

        super.onCreate(savedInstanceState)
        notificationService = NotificationService(applicationContext)
        setContent {
            ElaTheme {
//                ChatScreen(chatApi = chatApi)
                MainScreen()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        notificationService.showNotification(5, "WhatsApp")
    }
}


