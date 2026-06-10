package com.mool.feature.transactions

import com.mool.core.domain.Transaction
import com.mool.core.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class TransactionFormViewModel(
    private val repository: TransactionRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(TransactionFormState())
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()

    private val _effects = Channel<TransactionFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun accept(intent: TransactionFormIntent) {
        when (intent) {
            is TransactionFormIntent.SetAmount -> _state.update { it.copy(amount = intent.value) }
            is TransactionFormIntent.SetCurrency -> _state.update { it.copy(currency = intent.value) }
            is TransactionFormIntent.SetDescription -> _state.update { it.copy(description = intent.value) }
            is TransactionFormIntent.SetType -> _state.update { it.copy(type = intent.type) }
            is TransactionFormIntent.SetCategory -> _state.update { it.copy(category = intent.value) }
            is TransactionFormIntent.Submit -> submit()
            is TransactionFormIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    fun reset() {
        _state.update { TransactionFormState() }
    }

    private fun submit() {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            val s = _state.value
            val amount = s.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _state.update { it.copy(isSubmitting = false, error = "Amount must be a positive number") }
                return@launch
            }
            try {
                repository.addTransaction(
                    Transaction(
                        id = 0,
                        amount = amount,
                        currency = s.currency,
                        description = s.description,
                        type = s.type,
                        category = s.category,
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                    )
                )
                _state.update { it.copy(isSubmitting = false) }
                _effects.send(TransactionFormEffect.TransactionSaved)
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, error = e.message) }
                _effects.send(TransactionFormEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }
}
