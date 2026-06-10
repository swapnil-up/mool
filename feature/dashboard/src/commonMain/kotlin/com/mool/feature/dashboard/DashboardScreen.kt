package com.mool.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mool.core.domain.ExchangeRate
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
                is DashboardEffect.ShowError -> { /* snackbar/toast in later iteration */ }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Net Balance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "${currencySymbol(state.preferredCurrency)}${String.format("%.2f", state.balance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("USD Exchange Rates", style = MaterialTheme.typography.headlineMedium)
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(onClick = { viewModel.accept(DashboardIntent.Refresh) }) {
                    Text("Refresh")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text(
                    text = state.error!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Currency", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text("Rate", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(4.dp))

        LazyColumn {
            items(state.rates, key = { it.toCurrency }) { rate ->
                RateRow(rate)
            }
        }
    }
}

@Composable
private fun RateRow(rate: ExchangeRate) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(rate.toCurrency, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = String.format("%.4f", rate.rate),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun currencySymbol(code: String): String = when (code) {
    "USD" -> "$"
    "EUR" -> "\u20AC"
    "GBP" -> "\u00A3"
    "JPY" -> "\u00A5"
    "INR", "NPR" -> "\u20B9"
    else -> "$code "
}
