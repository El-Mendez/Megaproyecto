package com.example.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prototype.services.NotificationService

class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationService = NotificationService(applicationContext)
        setContent {
            MainScreen()
        }
    }

    override fun onStop() {
        super.onStop()
        notificationService.showNotification(5, "WhatsApp")
    }
}


