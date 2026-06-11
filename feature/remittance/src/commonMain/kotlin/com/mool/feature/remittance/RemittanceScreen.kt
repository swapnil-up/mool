package com.mool.feature.remittance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.toFixed
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RemittanceScreen(viewModel: RemittanceViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.observeRates()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is RemittanceEffect.ShowError -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Remittance Calculator", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.sendAmount,
            onValueChange = { viewModel.accept(RemittanceIntent.SetSendAmount(it)) },
            label = { Text("Send Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CurrencySelector(
                label = "From",
                selected = state.sendCurrency,
                currencies = state.availableCurrencies,
                onSelect = { viewModel.accept(RemittanceIntent.SetSendCurrency(it)) },
                modifier = Modifier.weight(1f),
            )
            CurrencySelector(
                label = "To",
                selected = state.receiveCurrency,
                currencies = state.availableCurrencies,
                onSelect = { viewModel.accept(RemittanceIntent.SetReceiveCurrency(it)) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.feePercent,
                onValueChange = { viewModel.accept(RemittanceIntent.SetFeePercent(it)) },
                label = { Text("Fee %") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.marginPercent,
                onValueChange = { viewModel.accept(RemittanceIntent.SetMarginPercent(it)) },
                label = { Text("Margin %") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(20.dp))

        ErrorBanner(state.error, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

        Button(
            onClick = { viewModel.accept(RemittanceIntent.Calculate) },
            enabled = !state.isCalculating,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (state.isCalculating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Calculate")
            }
        }

        state.appliedRate?.let { rate ->
            Spacer(Modifier.height(20.dp))
            ResultCard(state, rate)
        }
    }
}

@Composable
private fun ResultCard(state: RemittanceState, rate: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rate: 1 ${state.sendCurrency} = ${rate.toFixed(4)} ${state.receiveCurrency}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            state.calculatedFee?.let { fee ->
                Text(
                    "Fee: -${fee.toFixed(2)} ${state.receiveCurrency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            state.calculatedMargin?.let { margin ->
                Text(
                    "Margin: -${margin.toFixed(2)} ${state.receiveCurrency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            Text(
                "You Receive",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "${state.calculatedReceiveAmount?.toFixed(2) ?: ""} ${state.receiveCurrency}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(
    label: String,
    selected: String,
    currencies: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onSelect(currency)
                        expanded = false
                    },
                )
            }
        }
    }
}
