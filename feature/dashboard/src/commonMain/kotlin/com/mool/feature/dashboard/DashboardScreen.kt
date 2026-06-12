package com.mool.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mool.core.domain.ExchangeRate
import com.mool.core.ui.BudgetRow
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolAmountText
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolDivider
import com.mool.core.ui.MoolEmptyState
import com.mool.core.ui.MoolSectionHeader
import com.mool.core.ui.MoolShimmerCard
import com.mool.core.ui.SpendingBarChart
import com.mool.core.ui.SpendingPieChart
import com.mool.core.ui.toFixed
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRates()
        viewModel.observeBalance()
        viewModel.observePreferredCurrency()
        viewModel.observeCharts()
        viewModel.observeBudgets()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.ShowError -> {}
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.accept(DashboardIntent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        item {
            MoolCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(4.dp))
                    MoolAmountText(
                        amount = state.balance,
                        currency = state.preferredCurrency,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        if (state.spendingByCategory.isNotEmpty()) {
            item {
                MoolSectionHeader(title = "Spending This Month")
            }

            item {
                MoolCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoolAmountText(
                                    amount = state.monthTotal,
                                    currency = state.preferredCurrency,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoolAmountText(
                                    amount = state.monthIncome,
                                    currency = state.preferredCurrency,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        MoolDivider()
                        Spacer(Modifier.height(12.dp))
                        SpendingPieChart(
                            slices = state.spendingByCategory,
                            centerLabel = state.monthTotal.toFixed(2),
                        )
                    }
                }
            }
        }

        if (state.dailySpending.isNotEmpty()) {
            item {
                MoolSectionHeader(title = "Daily Spending")
            }

            item {
                MoolCard(modifier = Modifier.fillMaxWidth()) {
                    SpendingBarChart(
                        bars = state.dailySpending,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }

        if (state.budgets.isNotEmpty()) {
            item {
                MoolSectionHeader(title = "Budgets")
            }

            item {
                MoolCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.budgets.forEachIndexed { index, budget ->
                            if (index > 0) Spacer(Modifier.height(12.dp))
                            BudgetRow(
                                label = budget.category,
                                spent = budget.spent.toFloat(),
                                budget = budget.budget.toFloat(),
                            )
                        }
                    }
                }
            }
        }

        item {
            MoolSectionHeader(
                title = "Exchange Rates",
                trailing = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = { viewModel.accept(DashboardIntent.Refresh) }) {
                            Text("Refresh")
                        }
                    }
                },
            )
        }

        item {
            ErrorBanner(state.error, modifier = Modifier.fillMaxWidth())
        }

        if (state.rates.isEmpty() && !state.isLoading) {
            item {
                MoolEmptyState(
                    title = "No rates available",
                    subtitle = "Pull refresh or check your connection",
                    action = {
                        Button(
                            onClick = { viewModel.accept(DashboardIntent.Refresh) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Refresh")
                        }
                    },
                )
            }
        }

        if (state.isLoading && state.rates.isEmpty()) {
            items(3) {
                MoolShimmerCard()
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Currency",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Rate",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(state.rates, key = { it.toCurrency }) { rate ->
                RateRow(rate)
            }
        }
        }
    }
}

@Composable
private fun RateRow(rate: ExchangeRate) {
    MoolCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = rate.toCurrency,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = rate.rate.toFixed(4),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
