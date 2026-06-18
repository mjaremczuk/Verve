package com.mjaremczuk.motiv.domain.model

data class Quote(
    val text: String,
    val author: String,
    val html: String,
    val category: String = "stoic"
)
