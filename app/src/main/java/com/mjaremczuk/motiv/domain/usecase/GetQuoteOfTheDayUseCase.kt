package com.mjaremczuk.motiv.domain.usecase

import com.mjaremczuk.motiv.domain.model.Quote
import com.mjaremczuk.motiv.domain.repository.QuoteRepository

class GetQuoteOfTheDayUseCase(private val quoteRepository: QuoteRepository) {
    suspend operator fun invoke(): Pair<Quote?, Boolean> {
        return quoteRepository.getQuoteOfTheDayWithStatus()
    }
}
