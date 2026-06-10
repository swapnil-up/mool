package com.mool.feature.settings

import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    fun accept(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observePreference()
            is SettingsIntent.SetCurrency -> setCurrency(intent.currency)
        }
    }

    fun observePreference() {
        observeJob?.cancel()
        observeJob = scope.launch {
            settingsRepository.observeSetting(SettingsKeys.PREFERRED_CURRENCY).collect { value ->
                _state.update { it.copy(preferredCurrency = value ?: "USD", isLoading = false) }
            }
        }
    }

    private fun setCurrency(currency: String) {
        scope.launch {
            try {
                settingsRepository.setSetting(SettingsKeys.PREFERRED_CURRENCY, currency)
            } catch (e: Exception) {
                _effects.send(SettingsEffect.ShowError(e.message ?: "Failed to save preference"))
            }
        }
    }
}
