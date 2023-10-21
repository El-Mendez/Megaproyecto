package me.mendez.ela.chat

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.mendez.ela.BuildConfig
import me.mendez.ela.chat.dto.ChatGPTRequest
import me.mendez.ela.chat.dto.ChatGPTResponse
import me.mendez.ela.chat.dto.wrapForTransfer
import javax.inject.Singleton

private const val TAG = "ELA_CHAT_API"

class ChatApi(
    private val client: HttpClient,
) {
    suspend fun answer(chat: List<Message>): List<Message> {
        return try {
            Log.i(TAG, "trying to send message")
            getNewChatMessage(chat)
        } catch (e: Exception) {
            Log.e(TAG, "could not send message $e")
            emptyList()
        }
    }

    private suspend fun getNewChatMessage(oldMessages: List<Message>): List<Message> {
        val body = oldMessages.wrapForTransfer()

        val request = ChatGPTRequest(
            model = MODEL,
            messages = body,
            temperature = 0.7f,
        )

        val response: ChatGPTResponse = client.post {
            url(CHAT_URL)
            contentType(ContentType.Application.Json)
            bearerAuth(BuildConfig.GPT_API_KEY)
            setBody(request)
        }.body()

        return response.unwrap()
    }

    companion object {
        private const val BASE_URL = "https://api.openai.com/v1"
        private const val CHAT_URL = "$BASE_URL/chat/completions"
        private const val MODEL = "ft:gpt-3.5-turbo-0613:ela:ela:8BkjWG5l"

        fun create(): ChatApi {
            return ChatApi(
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
