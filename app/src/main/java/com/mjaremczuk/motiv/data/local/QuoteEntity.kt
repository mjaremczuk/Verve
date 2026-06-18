package com.mjaremczuk.motiv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mjaremczuk.motiv.domain.model.Quote

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey
    val text: String,
    val author: String,
    val html: String,
    val category: String = "stoic",
    val isDaily: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun QuoteEntity.toQuote() = Quote(
    text = text,
    author = author,
    html = html,
    category = category
)

fun Quote.toEntity(isDaily: Boolean = false) = QuoteEntity(
    text = text,
    author = author,
    html = html,
    category = category,
    isDaily = isDaily
)
