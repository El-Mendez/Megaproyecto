package com.example.ela.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val message: ChatMessage,
)

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)
