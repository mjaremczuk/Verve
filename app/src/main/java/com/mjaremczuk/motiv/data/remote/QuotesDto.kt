package com.mjaremczuk.motiv.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.mjaremczuk.motiv.domain.model.Quote

@JsonClass(generateAdapter = true)
data class QuoteDto(
    @param:Json(name = "text")
    val text: String,
    @param:Json(name = "author")
    val author: String
)

fun QuoteDto.toDomain() = Quote(
    text = text,
    author = author,
    html = ""
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @param:Json(name = "key")
    val key: String,
    @param:Json(name = "displayName")
    val displayName: String
)
