package com.mool.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mool.core.domain.ExchangeRate
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolAmountText
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolDivider
import com.mool.core.ui.MoolEmptyState
import com.mool.core.ui.MoolSectionHeader
import com.mool.core.ui.MoolShimmerCard
import com.mool.core.ui.toFixed
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRates()
        viewModel.observeBalance()
        viewModel.observePreferredCurrency()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.ShowError -> {}
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            // Balance Card
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
            // Column headers
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
