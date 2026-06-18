package com.mjaremczuk.motiv.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface QuotesApi {
    @GET("gh/mjaremczuk/Verve@main/docs/api/quotes/{category}.json")
    suspend fun getQuotesByCategory(@Path("category") category: String): List<QuoteDto>

    @GET("gh/mjaremczuk/Verve@main/docs/api/quotes/categories.json")
    suspend fun getCategories(): List<CategoryDto>
}
