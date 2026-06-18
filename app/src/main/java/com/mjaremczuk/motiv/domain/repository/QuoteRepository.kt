package com.mjaremczuk.motiv.domain.repository

import com.mjaremczuk.motiv.domain.model.Quote

interface QuoteRepository {
    suspend fun getRandomQuoteWithStatus(): Pair<Quote?, Boolean>
    suspend fun getQuoteOfTheDayWithStatus(): Pair<Quote?, Boolean>
    suspend fun getCategories(): List<Pair<String, String>>
}
