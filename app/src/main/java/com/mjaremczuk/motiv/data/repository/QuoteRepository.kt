package com.mjaremczuk.motiv.data.repository

import android.content.Context
import com.mjaremczuk.motiv.data.local.*
import com.mjaremczuk.motiv.data.model.Quote
import com.mjaremczuk.motiv.data.remote.ZenQuotesApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class QuoteRepository(context: Context) {

    private val quoteDao = AppDatabase.getDatabase(context).quoteDao()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(ZenQuotesApi.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ZenQuotesApi::class.java)

    private val fallbackQuotes = listOf(
        Quote("The only way to do great work is to love what you do.", "Steve Jobs", ""),
        Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", ""),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", ""),
        Quote("Your time is limited, so don't waste it living someone else's life.", "Steve Jobs", ""),
        Quote("Stay hungry, stay foolish.", "Steve Jobs", "")
    )

    suspend fun getRandomQuoteWithStatus(): Pair<Quote?, Boolean> {
        return try {
            val remoteQuote = api.getRandomQuote().firstOrNull()
            if (remoteQuote != null) {
                quoteDao.insertQuote(remoteQuote.toEntity(isDaily = false))
                Pair(remoteQuote, false)
            } else {
                Pair(getCachedOrFallbackRandomQuote(), true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(getCachedOrFallbackRandomQuote(), true)
        }
    }

    private suspend fun getCachedOrFallbackRandomQuote(): Quote {
        return quoteDao.getRandomQuote()?.toQuote() ?: fallbackQuotes.random()
    }

    suspend fun getQuoteOfTheDayWithStatus(): Pair<Quote?, Boolean> {
        return try {
            val remoteQuote = api.getQuoteOfTheDay().firstOrNull()
            if (remoteQuote != null) {
                quoteDao.updateDailyQuote(remoteQuote.toEntity(isDaily = true))
                Pair(remoteQuote, false)
            } else {
                Pair(getCachedOrFallbackDailyQuote(), true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(getCachedOrFallbackDailyQuote(), true)
        }
    }

    private suspend fun getCachedOrFallbackDailyQuote(): Quote {
        return quoteDao.getQuoteOfTheDay()?.toQuote() ?: fallbackQuotes.random()
    }

    // Keep original methods for backward compatibility if needed, but update implementation
    suspend fun getRandomQuote(): Quote? = getRandomQuoteWithStatus().first
    suspend fun getQuoteOfTheDay(): Quote? = getQuoteOfTheDayWithStatus().first
}
