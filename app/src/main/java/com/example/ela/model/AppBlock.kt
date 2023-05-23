package com.example.ela.model

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

data class AppBlock(
    val name: String,
    val blocks: List<InternetBlock>,
    val appImage: Drawable,
)
