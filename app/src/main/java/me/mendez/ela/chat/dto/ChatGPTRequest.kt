package me.mendez.ela.chat.dto

import kotlinx.serialization.Serializable


@Serializable
internal data class ChatGPTRequest(
    val model: String,
    val messages: List<MessageGPTRequest>,
    val temperature: Float,
)
