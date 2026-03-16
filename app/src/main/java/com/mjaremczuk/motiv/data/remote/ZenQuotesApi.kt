package com.mjaremczuk.motiv.data.remote

import com.mjaremczuk.motiv.data.model.Quote
import retrofit2.http.GET

interface ZenQuotesApi {
    @GET("random")
    suspend fun getRandomQuote(): List<Quote>

    @GET("today")
    suspend fun getQuoteOfTheDay(): List<Quote>

    companion object {
        const val BASE_URL = "https://zenquotes.io/api/"
    }
}
