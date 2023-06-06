package com.example.ela.remote

import com.example.ela.data.MessageData
import com.example.ela.remote.dto.ChatApiImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface ChatApi {
    suspend fun getResponse(chat: List<MessageData>): List<MessageData>

    companion object {
        fun create(): ChatApi {
            return ChatApiImpl(
                client = HttpClient(Android) {
                    install(Logging)
                    install(ContentNegotiation) {
                        json(Json {
                            prettyPrint = true
                            isLenient = true
                        })
                    }
                }
            )
        }
    }
}