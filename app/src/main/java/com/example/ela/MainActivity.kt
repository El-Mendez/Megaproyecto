package com.example.ela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ela.services.NotificationService
import com.example.ela.ui.theme.ElaTheme

class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationService = NotificationService(applicationContext)
        setContent {
            ElaTheme {
                MainScreen()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        notificationService.showNotification(5, "WhatsApp")
    }
}


