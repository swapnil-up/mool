package com.mool.feature.transactions

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

class TransactionHistoryViewModel(
    private val repository: TransactionRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(TransactionHistoryState())
    val state: StateFlow<TransactionHistoryState> = _state.asStateFlow()

    private val _effects = Channel<TransactionHistoryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    fun startObserving() {
        observeJob?.cancel()
        observeJob = scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.observeTransactions().collect { transactions ->
                    _state.update { it.copy(isLoading = false, transactions = transactions) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effects.send(TransactionHistoryEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun accept(intent: TransactionHistoryIntent) {
        when (intent) {
            TransactionHistoryIntent.Refresh -> startObserving()
            is TransactionHistoryIntent.DeleteTransaction -> delete(intent.id)
        }
    }

    private fun delete(id: Long) {
        scope.launch {
            try {
                repository.deleteTransaction(id)
            } catch (e: Exception) {
                _effects.send(TransactionHistoryEffect.ShowError(e.message ?: "Delete failed"))
            }
        }
    }
}
