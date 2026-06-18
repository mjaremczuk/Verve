package com.mjaremczuk.motiv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mjaremczuk.motiv.data.local.NotificationWorker
import com.mjaremczuk.motiv.domain.model.Quote
import com.mjaremczuk.motiv.domain.usecase.GetCategoriesUseCase
import com.mjaremczuk.motiv.domain.usecase.GetNotificationSettingsUseCase
import com.mjaremczuk.motiv.domain.usecase.GetQuoteOfTheDayUseCase
import com.mjaremczuk.motiv.domain.usecase.GetRandomQuoteUseCase
import com.mjaremczuk.motiv.domain.usecase.ToggleNotificationsUseCase
import com.mjaremczuk.motiv.domain.usecase.UpdateNotificationTimeUseCase
import com.mjaremczuk.motiv.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuoteUiState {
    object Loading : QuoteUiState()
    data class Success(val quote: Quote, val isOffline: Boolean = false) : QuoteUiState()
    data class Error(val message: String) : QuoteUiState()
}

class QuoteViewModel(
    private val getQuoteOfTheDayUseCase: GetQuoteOfTheDayUseCase,
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase,
    private val updateNotificationTimeUseCase: UpdateNotificationTimeUseCase,
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    // History management
    private val history = mutableListOf<QuoteUiState.Success>()
    private var currentIndex = -1

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    // Preferences / Settings from Use Case
    val notificationsEnabled: StateFlow<Boolean> = getNotificationSettingsUseCase.notificationsEnabled
    val notificationHour: StateFlow<Int> = getNotificationSettingsUseCase.notificationHour
    val notificationMinute: StateFlow<Int> = getNotificationSettingsUseCase.notificationMinute
    val selectedCategory: StateFlow<String> = settingsRepository.selectedCategory

    private val _categories = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "stoic" to "Stoic",
            "motivation" to "Motivation",
            "programming" to "Coding",
            "mindfulness" to "Mindfulness",
            "wisdom" to "Wisdom",
            "success" to "Success"
        )
    )
    val categories: StateFlow<List<Pair<String, String>>> = _categories.asStateFlow()

    fun selectCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
            fetchQuoteOfTheDay()
        }
    }

    init {
        fetchCategories()
        fetchQuoteOfTheDay()
        if (notificationsEnabled.value) {
            NotificationWorker.scheduleNextDailyNotification(application)
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            val list = getCategoriesUseCase()
            _categories.value = list
        }
    }

    fun fetchQuoteOfTheDay() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            val (quote, isOffline) = getQuoteOfTheDayUseCase()
            if (quote != null) {
                val successState = QuoteUiState.Success(quote, isOffline)
                history.clear()
                history.add(successState)
                currentIndex = 0
                _uiState.value = successState
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch quote. Please check your connection.")
            }
            _canGoBack.value = currentIndex > 0
        }
    }

    fun fetchRandomQuote() {
        viewModelScope.launch {
            if (currentIndex < history.lastIndex) {
                currentIndex++
                _uiState.value = history[currentIndex]
                _canGoBack.value = currentIndex > 0
                return@launch
            }

            _uiState.value = QuoteUiState.Loading
            val (quote, isOffline) = getRandomQuoteUseCase()
            if (quote != null) {
                val successState = QuoteUiState.Success(quote, isOffline)
                history.add(successState)
                currentIndex = history.lastIndex
                _uiState.value = successState
            } else {
                _uiState.value = QuoteUiState.Error("Failed to fetch quote. Please check your connection.")
            }
            _canGoBack.value = currentIndex > 0
        }
    }

    fun goBack() {
        if (currentIndex > 0) {
            currentIndex--
            _uiState.value = history[currentIndex]
            _canGoBack.value = currentIndex > 0
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        toggleNotificationsUseCase(enabled)
        if (enabled) {
            NotificationWorker.scheduleNextDailyNotification(getApplication())
        } else {
            NotificationWorker.cancelDailyNotification(getApplication())
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        updateNotificationTimeUseCase(hour, minute)
        if (notificationsEnabled.value) {
            NotificationWorker.scheduleNextDailyNotification(getApplication())
        }
    }
}
