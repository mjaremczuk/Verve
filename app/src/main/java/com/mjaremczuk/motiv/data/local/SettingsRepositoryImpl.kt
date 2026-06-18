package com.mjaremczuk.motiv.data.local

import android.content.Context
import com.mjaremczuk.motiv.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences("verve_prefs", Context.MODE_PRIVATE)

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", false))
    override val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationHour = MutableStateFlow(prefs.getInt("notification_hour", 8))
    override val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(prefs.getInt("notification_minute", 0))
    override val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    private val _selectedCategory = MutableStateFlow(prefs.getString("selected_category", "stoic") ?: "stoic")
    override val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    override fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    override fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt("notification_hour", hour)
            .putInt("notification_minute", minute)
            .apply()
        _notificationHour.value = hour
        _notificationMinute.value = minute
    }

    override fun setSelectedCategory(category: String) {
        prefs.edit().putString("selected_category", category).apply()
        _selectedCategory.value = category
    }
}
