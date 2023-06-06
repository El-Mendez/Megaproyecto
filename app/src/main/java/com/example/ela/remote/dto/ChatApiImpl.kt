package com.example.ela.remote.dto

import com.example.ela.data.MessageData
import com.example.ela.remote.ChatApi
import com.example.ela.remote.GPTRoutes

import io.ktor.client.request.url
import io.ktor.http.contentType

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType

class ChatApiImpl(
    private val client: HttpClient
): ChatApi {
    override suspend fun getResponse(chat: List<MessageData>): List<MessageData> {
        val reqBody = chat.map {
            ChatMessage(
                if (it.userCreated) "user" else  "assistant",
                it.content,
            )
        }

        return try {
            getNewChatMessage(reqBody).choices.map {
                MessageData(
                    content = it.message.content,
                    userCreated = it.message.role != "assistant"
                )
            }
        } catch (e: ClientRequestException) {
            println("API ERROR ${e.message}")
            emptyList()
        }
    }

    private suspend fun getNewChatMessage(chat: List<ChatMessage>): ChatResponse {
        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = chat,
            temperature = 0.7f,
        )
        return client.post {
            url(GPTRoutes.CHAT)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

