package com.mool.feature.remittance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolSectionHeader
import com.mool.core.ui.MoolShimmerCard
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
        MoolSectionHeader(title = "Remittance Calculator")
        Spacer(Modifier.height(20.dp))

        if (state.isRatesLoading && state.availableCurrencies.isEmpty()) {
            MoolShimmerCard()
            Spacer(Modifier.height(12.dp))
            MoolShimmerCard()
        } else {
            RemittanceForm(state, viewModel)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RemittanceForm(state: RemittanceState, viewModel: RemittanceViewModel) {
    SendAmountCard(state, viewModel)

    Spacer(Modifier.height(12.dp))

    FeeMarginCard(state, viewModel)

    Spacer(Modifier.height(20.dp))

    ErrorBanner(state.error, modifier = Modifier.fillMaxWidth())

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = { viewModel.accept(RemittanceIntent.Calculate) },
        enabled = !state.isCalculating,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (state.isCalculating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text("Calculate", style = MaterialTheme.typography.labelLarge)
        }
    }

    AnimatedVisibility(
        visible = state.appliedRate != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        state.appliedRate?.let { rate ->
            Spacer(Modifier.height(20.dp))
            ResultCard(state, rate)
        }
    }
}

@Composable
private fun SendAmountCard(state: RemittanceState, viewModel: RemittanceViewModel) {
    MoolCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            MoolOutlinedField(
                value = state.sendAmount,
                onValueChange = { viewModel.accept(RemittanceIntent.SetSendAmount(it)) },
                label = "Send Amount",
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CurrencySelector(
                    label = "From",
                    selected = state.sendCurrency,
                    currencies = state.availableCurrencies,
                    onSelect = { viewModel.accept(RemittanceIntent.SetSendCurrency(it)) },
                    modifier = Modifier.weight(1f),
                )

                FilledTonalButton(
                    onClick = {
                        val temp = state.sendCurrency
                        viewModel.accept(RemittanceIntent.SetSendCurrency(state.receiveCurrency))
                        viewModel.accept(RemittanceIntent.SetReceiveCurrency(temp))
                    },
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text("⇄", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                CurrencySelector(
                    label = "To",
                    selected = state.receiveCurrency,
                    currencies = state.availableCurrencies,
                    onSelect = { viewModel.accept(RemittanceIntent.SetReceiveCurrency(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MoolOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun FeeMarginCard(state: RemittanceState, viewModel: RemittanceViewModel) {
    MoolCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeeField(
                    value = state.feePercent,
                    onValueChange = { viewModel.accept(RemittanceIntent.SetFeePercent(it)) },
                    label = "Fee %",
                    modifier = Modifier.weight(1f),
                )
                FeeField(
                    value = state.marginPercent,
                    onValueChange = { viewModel.accept(RemittanceIntent.SetMarginPercent(it)) },
                    label = "Margin %",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FeeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun ResultCard(state: RemittanceState, rate: Double) {
    MoolCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Rate: 1 ${state.sendCurrency} = ${rate.toFixed(4)} ${state.receiveCurrency}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            state.calculatedFee?.let { fee ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Fee", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("-${fee.toFixed(2)} ${state.receiveCurrency}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            state.calculatedMargin?.let { margin ->
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Margin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("-${margin.toFixed(2)} ${state.receiveCurrency}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))
            Text("You Receive", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
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
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
