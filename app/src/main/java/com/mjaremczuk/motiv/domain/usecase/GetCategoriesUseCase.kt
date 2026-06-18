package com.mjaremczuk.motiv.domain.usecase

import com.mjaremczuk.motiv.domain.repository.QuoteRepository

class GetCategoriesUseCase(private val quoteRepository: QuoteRepository) {
    suspend operator fun invoke(): List<Pair<String, String>> {
        return quoteRepository.getCategories()
    }
}
