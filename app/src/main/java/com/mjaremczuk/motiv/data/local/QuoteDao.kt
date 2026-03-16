package com.mjaremczuk.motiv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE isDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE isDaily = 1 LIMIT 1")
    suspend fun getQuoteOfTheDay(): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE isDaily = :isDaily")
    suspend fun deleteQuotesByType(isDaily: Boolean)

    @Transaction
    suspend fun updateDailyQuote(quote: QuoteEntity) {
        deleteQuotesByType(true)
        insertQuote(quote.copy(isDaily = true))
    }
}
