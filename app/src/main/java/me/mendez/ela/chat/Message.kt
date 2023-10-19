package me.mendez.ela.chat

import java.util.Date


data class Message(
    val content: String,
    val userCreated: Boolean,
    val date: Date = Date(),
)
