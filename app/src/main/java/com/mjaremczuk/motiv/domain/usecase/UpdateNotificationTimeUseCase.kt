package com.mjaremczuk.motiv.domain.usecase

import com.mjaremczuk.motiv.domain.repository.SettingsRepository

class UpdateNotificationTimeUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(hour: Int, minute: Int) {
        settingsRepository.setNotificationTime(hour, minute)
    }
}
