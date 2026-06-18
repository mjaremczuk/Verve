package com.mjaremczuk.motiv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE isDaily = 0 AND category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(category: String): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE isDaily = 1 AND category = :category LIMIT 1")
    suspend fun getQuoteOfTheDay(category: String): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE isDaily = :isDaily AND category = :category")
    suspend fun deleteQuotesByTypeAndCategory(isDaily: Boolean, category: String)

    @Transaction
    suspend fun updateDailyQuote(quote: QuoteEntity) {
        deleteQuotesByTypeAndCategory(true, quote.category)
        insertQuote(quote.copy(isDaily = true))
    }
}
