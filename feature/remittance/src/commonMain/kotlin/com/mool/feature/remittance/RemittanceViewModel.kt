package com.mool.feature.remittance

import com.mool.core.domain.ExchangeRate
import com.mool.core.domain.repository.ExchangeRateRepository
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

class RemittanceViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(RemittanceState())
    val state: StateFlow<RemittanceState> = _state.asStateFlow()

    private val _effects = Channel<RemittanceEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var ratesJob: Job? = null
    private var cachedRates: List<ExchangeRate> = emptyList()

    fun observeRates() {
        if (ratesJob?.isActive == true) return
        ratesJob = scope.launch {
            _state.update { it.copy(isRatesLoading = true) }
            try {
                exchangeRateRepository.observeRates().collect { rates ->
                    cachedRates = rates
                    val currencies = rates.map { it.toCurrency }.distinct().sorted()
                    _state.update { it.copy(isRatesLoading = false, availableCurrencies = currencies) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isRatesLoading = false, error = e.message) }
                _effects.send(RemittanceEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun accept(intent: RemittanceIntent) {
        when (intent) {
            is RemittanceIntent.SetSendAmount -> _state.update { it.copy(sendAmount = intent.value) }
            is RemittanceIntent.SetSendCurrency -> _state.update { it.copy(sendCurrency = intent.value) }
            is RemittanceIntent.SetReceiveCurrency -> _state.update { it.copy(receiveCurrency = intent.value) }
            is RemittanceIntent.SetFeePercent -> _state.update { it.copy(feePercent = intent.value) }
            is RemittanceIntent.SetMarginPercent -> _state.update { it.copy(marginPercent = intent.value) }
            is RemittanceIntent.Calculate -> calculate()
            is RemittanceIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun calculate() {
        scope.launch {
            _state.update { it.copy(isCalculating = true, error = null) }
            val s = _state.value

            val amount = s.sendAmount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _state.update { it.copy(isCalculating = false, error = "Enter a valid send amount") }
                return@launch
            }

            val rate = findRate(s.sendCurrency, s.receiveCurrency)
            if (rate == null) {
                _state.update { it.copy(isCalculating = false, error = "Rate unavailable for ${s.sendCurrency} → ${s.receiveCurrency}") }
                return@launch
            }

            val feePct = s.feePercent.toDoubleOrNull() ?: 0.0
            val marginPct = s.marginPercent.toDoubleOrNull() ?: 0.0

            val converted = amount * rate
            val fee = converted * feePct / 100.0
            val margin = converted * marginPct / 100.0
            val receive = converted - fee - margin

            _state.update {
                it.copy(
                    isCalculating = false,
                    appliedRate = rate,
                    calculatedFee = fee,
                    calculatedMargin = margin,
                    calculatedReceiveAmount = receive,
                )
            }
        }
    }

    private fun findRate(from: String, to: String): Double? {
        if (from == to) return 1.0
        val fromRate = cachedRates.find { it.fromCurrency == "USD" && it.toCurrency == from }?.rate
        val toRate = cachedRates.find { it.fromCurrency == "USD" && it.toCurrency == to }?.rate
        return when {
            from == "USD" -> toRate
            to == "USD" -> if (fromRate != null) 1.0 / fromRate else null
            fromRate != null && toRate != null -> toRate / fromRate
            else -> null
        }
    }
}
