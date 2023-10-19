package me.mendez.ela.remote

import me.mendez.ela.chat.Message
import me.mendez.ela.remote.dto.ChatApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

interface ChatApi {
    suspend fun getResponse(chat: List<Message>): List<Message>

    companion object {
        fun create(): ChatApi {
            return ChatApiImpl(
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.ALL
                    }
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        })
                    }
                }
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ChatApiModel {
    @Provides
    @Singleton
    fun provideChatApi() = ChatApi.create()
}
