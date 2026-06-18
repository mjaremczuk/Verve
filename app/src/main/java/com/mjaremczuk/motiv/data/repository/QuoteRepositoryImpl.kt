package com.mjaremczuk.motiv.data.repository

import android.util.Log
import com.mjaremczuk.motiv.data.local.QuoteDao
import com.mjaremczuk.motiv.data.local.toEntity
import com.mjaremczuk.motiv.data.local.toQuote
import com.mjaremczuk.motiv.data.remote.QuotesApi
import com.mjaremczuk.motiv.data.remote.toDomain
import com.mjaremczuk.motiv.domain.model.Quote
import com.mjaremczuk.motiv.domain.repository.QuoteRepository
import com.mjaremczuk.motiv.domain.repository.SettingsRepository

class QuoteRepositoryImpl(
    private val quoteDao: QuoteDao,
    private val api: QuotesApi,
    private val settingsRepository: SettingsRepository
) : QuoteRepository {

    companion object {
        private const val TAG = "QuoteRepository"
    }

    private val fallbackQuotes = mapOf(
        "stoic" to listOf(
            Quote("It is not the man who has too little who is poor, but the one who hankers after more.", "Seneca", "", "stoic"),
            Quote("You have power over your mind - not outside events. Realize this, and you will find strength.", "Marcus Aurelius", "", "stoic"),
            Quote("Difficulty shows what men are.", "Epictetus", "", "stoic"),
            Quote("Waste no more time arguing about what a good man should be. Be one.", "Marcus Aurelius", "", "stoic"),
            Quote("We suffer more often in imagination than in reality.", "Seneca", "", "stoic"),
            Quote("He who fears death will never do anything worth of a man who is alive.", "Seneca", "", "stoic"),
            Quote("The best revenge is to be unlike him who performed the injury.", "Marcus Aurelius", "", "stoic"),
            Quote("If it is not right do not do it; if it is not true do not say it.", "Marcus Aurelius", "", "stoic"),
            Quote("No man is free who is not master of himself.", "Epictetus", "", "stoic"),
            Quote("Associate with people who are likely to improve you.", "Seneca", "", "stoic")
        ),
        "motivation" to listOf(
            Quote("The only way to do great work is to love what you do.", "Steve Jobs", "", "motivation"),
            Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "", "motivation"),
            Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", "", "motivation"),
            Quote("Your time is limited, so don't waste it living someone else's life.", "Steve Jobs", "", "motivation"),
            Quote("Do what you can, with what you have, where you are.", "Theodore Roosevelt", "", "motivation"),
            Quote("It always seems impossible until it's done.", "Nelson Mandela", "", "motivation"),
            Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson", "", "motivation"),
            Quote("The future depends on what you do today.", "Mahatma Gandhi", "", "motivation"),
            Quote("Start where you are. Use what you have. Do what you can.", "Arthur Ashe", "", "motivation"),
            Quote("Aim for the moon. If you miss, you may hit a star.", "W. Clement Stone", "", "motivation")
        ),
        "programming" to listOf(
            Quote("Talk is cheap. Show me the code.", "Linus Torvalds", "", "programming"),
            Quote("Programs must be written for people to read, and only incidentally for machines to execute.", "Harold Abelson", "", "programming"),
            Quote("Truth can only be found in one place: the code.", "Robert C. Martin", "", "programming"),
            Quote("Simplicity is the soul of efficiency.", "Austin Freeman", "", "programming"),
            Quote("First, solve the problem. Then, write the code.", "John Johnson", "", "programming"),
            Quote("Clean code always looks like it was written by someone who cares.", "Michael Feathers", "", "programming"),
            Quote("Any fool can write code that a computer can understand. Good programmers write code that humans can understand.", "Martin Fowler", "", "programming"),
            Quote("Indeed, the ratio of time spent reading vs. writing is over 10 to 1. We are constantly reading old code to write new code.", "Robert C. Martin", "", "programming"),
            Quote("Before software can be reusable it first has to be usable.", "Ralph Johnson", "", "programming"),
            Quote("Make it work, make it right, make it fast.", "Kent Beck", "", "programming")
        )
    )

    override suspend fun getRandomQuoteWithStatus(): Pair<Quote?, Boolean> {
        val category = settingsRepository.selectedCategory.value
        Log.d(TAG, "getRandomQuoteWithStatus() called for category: $category")
        return fetchQuoteWithStatus(
            category = category,
            saveLocal = { quote -> quoteDao.insertQuote(quote.toEntity(isDaily = false)) },
            getLocal = { quoteDao.getRandomQuote(category)?.toQuote() }
        )
    }

    override suspend fun getQuoteOfTheDayWithStatus(): Pair<Quote?, Boolean> {
        val category = settingsRepository.selectedCategory.value
        Log.d(TAG, "getQuoteOfTheDayWithStatus() called for category: $category")
        
        val cachedEntity = quoteDao.getQuoteOfTheDay(category)
        if (cachedEntity != null) {
            if (isSameDay(cachedEntity.timestamp, System.currentTimeMillis())) {
                Log.d(TAG, "getQuoteOfTheDayWithStatus: Cached quote of the day is valid for today. Returning cached quote: '${cachedEntity.text}' by ${cachedEntity.author}")
                return Pair(cachedEntity.toQuote(), true)
            }
        }

        Log.d(TAG, "getQuoteOfTheDayWithStatus: Cache missing or stale. Fetching new quote of the day from network...")
        return fetchQuoteWithStatus(
            category = category,
            saveLocal = { quote -> quoteDao.updateDailyQuote(quote.toEntity(isDaily = true)) },
            getLocal = { quoteDao.getQuoteOfTheDay(category)?.toQuote() }
        )
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /**
     * A generic helper encapsulating try-catch, remote fetch, local storage caching, 
     * and database/fallback recovery. This avoids repeating the same recovery blocks (DRY).
     */
    private suspend fun fetchQuoteWithStatus(
        category: String,
        saveLocal: suspend (Quote) -> Unit,
        getLocal: suspend () -> Quote?
    ): Pair<Quote?, Boolean> {
        Log.d(TAG, "fetchQuoteWithStatus: Starting network fetch for category '$category'...")
        return try {
            val remoteQuotes = api.getQuotesByCategory(category)
            if (remoteQuotes.isNotEmpty()) {
                Log.d(TAG, "fetchQuoteWithStatus: Network fetch succeeded. Retrieved ${remoteQuotes.size} quotes. Saving to local database...")
                val domainQuotes = remoteQuotes.map { it.toDomain().copy(category = category) }
                domainQuotes.forEach { saveLocal(it) }
                Pair(domainQuotes.random(), false)
            } else {
                Log.w(TAG, "fetchQuoteWithStatus: Network request returned empty list.")
                loadFallback(category, getLocal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchQuoteWithStatus: Network fetch failed with exception: ${e.message}", e)
            loadFallback(category, getLocal)
        }
    }

    private suspend fun loadFallback(category: String, getLocal: suspend () -> Quote?): Pair<Quote?, Boolean> {
        Log.d(TAG, "loadFallback: Attempting to retrieve quote from local database cache for category '$category'...")
        val cached = getLocal()
        return if (cached != null) {
            Log.d(TAG, "loadFallback: Successfully loaded cached quote from database: '${cached.text}' by ${cached.author}")
            Pair(cached, true)
        } else {
            val list = fallbackQuotes[category] ?: fallbackQuotes["stoic"]!!
            val hardcoded = list.random()
            Log.w(TAG, "loadFallback: No cached quotes found for category '$category'. Returning category-specific fallback: '${hardcoded.text}' by ${hardcoded.author}")
            Pair(hardcoded, true)
        }
    }

    override suspend fun getCategories(): List<Pair<String, String>> {
        return try {
            val list = api.getCategories()
            Log.d(TAG, "getCategories: Successfully loaded ${list.size} categories from network")
            list.map { it.key to it.displayName }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories: Failed to fetch categories from network. Returning default hardcoded categories.", e)
            listOf(
                "stoic" to "Stoic",
                "motivation" to "Motivation",
                "programming" to "Coding",
                "mindfulness" to "Mindfulness",
                "wisdom" to "Wisdom",
                "success" to "Success"
            )
        }
    }
}


