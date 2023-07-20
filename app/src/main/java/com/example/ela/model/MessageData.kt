package com.example.ela.model

import java.util.Date


data class MessageData(
    val content: String,
    val userCreated: Boolean,
    val date: Date,
)

