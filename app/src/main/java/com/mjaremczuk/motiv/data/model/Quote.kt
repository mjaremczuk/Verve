package com.mjaremczuk.motiv.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Quote(
    @Json(name = "q")
    val text: String,
    @Json(name = "a")
    val author: String,
    @Json(name = "h")
    val html: String
)
