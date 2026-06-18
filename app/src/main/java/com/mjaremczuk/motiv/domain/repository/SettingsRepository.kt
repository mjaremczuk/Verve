package com.mjaremczuk.motiv.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val notificationsEnabled: StateFlow<Boolean>
    val notificationHour: StateFlow<Int>
    val notificationMinute: StateFlow<Int>
    val selectedCategory: StateFlow<String>

    fun setNotificationsEnabled(enabled: Boolean)
    fun setNotificationTime(hour: Int, minute: Int)
    fun setSelectedCategory(category: String)
}
