package me.mendez.ela.remote

object GPTRoutes {
    private const val BASE_URL = "https://api.openai.com/v1"
    const val CHAT = "$BASE_URL/chat/completions"
}