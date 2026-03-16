package com.mjaremczuk.motiv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mjaremczuk.motiv.data.model.Quote
import com.mjaremczuk.motiv.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuoteUiState {
    object Loading : QuoteUiState()
    data class Success(val quote: Quote, val isOffline: Boolean = false) : QuoteUiState()
    data class Error(val message: String) : QuoteUiState()
}

class QuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)
    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    init {
        fetchQuoteOfTheDay()
    }

    fun fetchQuoteOfTheDay() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            val (quote, isOffline) = repository.getQuoteOfTheDayWithStatus()
            if (quote != null) {
                _uiState.value = QuoteUiState.Success(quote, isOffline)
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch quote. Please check your connection.")
            }
        }
    }

    fun fetchRandomQuote() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            val (quote, isOffline) = repository.getRandomQuoteWithStatus()
            if (quote != null) {
                _uiState.value = QuoteUiState.Success(quote, isOffline)
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch quote. Please check your connection.")
            }
        }
    }
}
