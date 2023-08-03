package me.mendez.ela.model

data class InternetBlock(
    val time: String,
    val domain: String,
    val reasons: List<String>,
)
