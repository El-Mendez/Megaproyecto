package com.example.ela.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float,
)