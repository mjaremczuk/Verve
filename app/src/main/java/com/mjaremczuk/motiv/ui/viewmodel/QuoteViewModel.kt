package com.mjaremczuk.motiv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjaremczuk.motiv.data.model.Quote
import com.mjaremczuk.motiv.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuoteUiState {
    object Loading : QuoteUiState()
    data class Success(val quote: Quote) : QuoteUiState()
    data class Error(val message: String) : QuoteUiState()
}

class QuoteViewModel(private val repository: QuoteRepository = QuoteRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    init {
        fetchQuoteOfTheDay()
    }

    fun fetchQuoteOfTheDay() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            val quote = repository.getQuoteOfTheDay()
            if (quote != null) {
                _uiState.value = QuoteUiState.Success(quote)
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch quote of the day. Please check your connection.")
            }
        }
    }

    fun fetchRandomQuote() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            val quote = repository.getRandomQuote()
            if (quote != null) {
                _uiState.value = QuoteUiState.Success(quote)
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch random quote. Please check your connection.")
            }
        }
    }
}
