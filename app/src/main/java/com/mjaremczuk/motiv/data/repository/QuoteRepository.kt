package com.mjaremczuk.motiv.data.repository

import com.mjaremczuk.motiv.data.model.Quote
import com.mjaremczuk.motiv.data.remote.ZenQuotesApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class QuoteRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(ZenQuotesApi.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ZenQuotesApi::class.java)

    suspend fun getRandomQuote(): Quote? {
        return try {
            api.getRandomQuote().firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getQuoteOfTheDay(): Quote? {
        return try {
            api.getQuoteOfTheDay().firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
