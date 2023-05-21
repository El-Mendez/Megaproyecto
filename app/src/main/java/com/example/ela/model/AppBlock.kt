package com.example.ela.model

import androidx.annotation.DrawableRes

data class AppBlock(
    val name: String,
    val blocks: List<InternetBlock>,
    @DrawableRes val appImage: Int,
)
