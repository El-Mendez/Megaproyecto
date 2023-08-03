package me.mendez.ela.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)
