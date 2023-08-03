package me.mendez.ela.model

import android.graphics.drawable.Drawable

data class AppBlock(
    val name: String,
    val blocks: List<InternetBlock>,
    val appImage: Drawable,
)
