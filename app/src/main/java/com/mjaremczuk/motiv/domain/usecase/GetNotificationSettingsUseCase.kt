package com.mjaremczuk.motiv.domain.usecase

import com.mjaremczuk.motiv.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class GetNotificationSettingsUseCase(private val settingsRepository: SettingsRepository) {
    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
    val notificationHour: StateFlow<Int> = settingsRepository.notificationHour
    val notificationMinute: StateFlow<Int> = settingsRepository.notificationMinute
}
