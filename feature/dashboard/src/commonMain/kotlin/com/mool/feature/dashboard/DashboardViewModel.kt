package com.mool.feature.dashboard

import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.TransactionType
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.domain.repository.TransactionRepository
import com.mool.core.ui.BarEntry
import com.mool.core.ui.PieSlice
import com.mool.core.ui.pickColor
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
    private var chartJob: Job? = null
    private var budgetsJob: Job? = null
    private var cachedExpensesByCategory: Map<String, List<com.mool.core.domain.Transaction>> = emptyMap()

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

    fun observeCharts() {
        chartJob?.cancel()
        chartJob = scope.launch {
            try {
                transactionRepository.observeTransactions().collect { transactions ->
                    val dayMs = 86_400_000L
                    val monthMs = 30L * dayMs
                    val nowMs = transactions.maxOfOrNull { it.timestamp } ?: return@collect

                    val monthStartMs = nowMs - monthMs

                    val monthTxs = transactions.filter { it.timestamp >= monthStartMs }
                    val expenses = monthTxs.filter { it.type == TransactionType.EXPENSE }
                    val income = monthTxs.filter { it.type == TransactionType.INCOME }

                    val monthTotal = expenses.sumOf { it.amount }
                    val monthIncome = income.sumOf { it.amount }

                    cachedExpensesByCategory = expenses.groupBy { it.category.ifEmpty { "Uncategorized" } }
                    val spendingByCategory = cachedExpensesByCategory.entries.mapIndexed { index, entry ->
                        PieSlice(
                            label = entry.key,
                            value = entry.value.sumOf { it.amount }.toFloat(),
                            color = pickColor(index),
                        )
                    }.sortedByDescending { it.value }

                    val dailySpending = (6 downTo 0).map { daysAgo ->
                        val dayStart = nowMs - daysAgo * dayMs
                        val dayEnd = dayStart + dayMs
                        val dayTotal = expenses.filter { tx ->
                            tx.timestamp in dayStart until dayEnd
                        }.sumOf { it.amount }
                        val label = when (daysAgo) {
                            0 -> "Today"
                            1 -> "1d"
                            else -> "${daysAgo}d"
                        }
                        BarEntry(
                            label = label,
                            value = dayTotal.toFloat(),
                            color = pickColor(0),
                        )
                    }

                    val currentBudgets = _state.value.budgets
                    _state.update {
                        it.copy(
                            spendingByCategory = spendingByCategory,
                            dailySpending = dailySpending,
                            monthTotal = monthTotal,
                            monthIncome = monthIncome,
                            budgets = currentBudgets,
                        )
                    }
                }
            } catch (e: Exception) {
                _effects.send(DashboardEffect.ShowError(e.message ?: "Failed to load charts"))
            }
        }
    }

    fun observeBudgets() {
        budgetsJob?.cancel()
        budgetsJob = scope.launch {
            val budgetCategories = listOf("Food", "Transport", "Shopping", "Bills")
            budgetCategories.forEach { category ->
                launch {
                    val key = "budget_${category.lowercase()}"
                    settingsRepository.observeSetting(key).collect { value ->
                        val budget = value?.toDoubleOrNull() ?: 0.0
                        val byCategory = cachedExpensesByCategory
                        val spent = byCategory[category]?.sumOf { it.amount } ?: 0.0
                        _state.update { s ->
                            val updated = s.budgets.toMutableList()
                            val idx = updated.indexOfFirst { it.category == category }
                            val info = BudgetInfo(category = category, spent = spent, budget = budget)
                            if (idx >= 0) updated[idx] = info else updated.add(info)
                            s.copy(budgets = updated)
                        }
                    }
                }
            }
        }
    }
}
