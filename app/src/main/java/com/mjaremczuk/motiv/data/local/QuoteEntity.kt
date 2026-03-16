package com.mjaremczuk.motiv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mjaremczuk.motiv.data.model.Quote

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey
    val text: String,
    val author: String,
    val html: String,
    val isDaily: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun QuoteEntity.toQuote() = Quote(
    text = text,
    author = author,
    html = html
)

fun Quote.toEntity(isDaily: Boolean = false) = QuoteEntity(
    text = text,
    author = author,
    html = html,
    isDaily = isDaily
)
