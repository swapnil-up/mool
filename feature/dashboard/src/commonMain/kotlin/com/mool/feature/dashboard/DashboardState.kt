package com.mool.feature.dashboard

import com.mool.core.domain.ExchangeRate
import com.mool.core.ui.BarEntry
import com.mool.core.ui.PieSlice

data class DashboardState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val preferredCurrency: String = "USD",
    val rates: List<ExchangeRate> = emptyList(),
    val error: String? = null,
    val spendingByCategory: List<PieSlice> = emptyList(),
    val dailySpending: List<BarEntry> = emptyList(),
    val monthTotal: Double = 0.0,
    val monthIncome: Double = 0.0,
    val budgets: List<BudgetInfo> = emptyList(),
)

data class BudgetInfo(
    val category: String,
    val spent: Double,
    val budget: Double,
)
