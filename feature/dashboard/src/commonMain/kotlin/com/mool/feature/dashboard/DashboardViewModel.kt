package com.mool.feature.dashboard

import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.domain.repository.TransactionRepository
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

class DashboardViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var ratesJob: Job? = null
    private var balanceJob: Job? = null
    private var settingsJob: Job? = null

    fun accept(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Refresh -> loadRates()
        }
    }

    fun loadRates() {
        ratesJob?.cancel()
        ratesJob = scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                exchangeRateRepository.refreshRates()
                exchangeRateRepository.observeRates().collect { rates ->
                    _state.update { it.copy(isLoading = false, rates = rates) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effects.send(DashboardEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun observeBalance() {
        balanceJob?.cancel()
        balanceJob = scope.launch {
            try {
                transactionRepository.observeBalance().collect { balance ->
                    _state.update { it.copy(balance = balance) }
                }
            } catch (e: Exception) {
                _effects.send(DashboardEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun observePreferredCurrency() {
        settingsJob?.cancel()
        settingsJob = scope.launch {
            try {
                settingsRepository.observeSetting(SettingsKeys.PREFERRED_CURRENCY).collect { value ->
                    _state.update { it.copy(preferredCurrency = value ?: "USD") }
                }
            } catch (e: Exception) {
                _effects.send(DashboardEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }
}
