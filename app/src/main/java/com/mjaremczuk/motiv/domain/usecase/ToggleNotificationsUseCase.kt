package com.mjaremczuk.motiv.domain.usecase

import com.mjaremczuk.motiv.domain.repository.SettingsRepository

class ToggleNotificationsUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
    }
}
