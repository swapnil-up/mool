package com.mool.feature.transactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mool.core.domain.TransactionType
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolSectionHeader
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TransactionFormScreen(viewModel: TransactionFormViewModel, onSaved: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TransactionFormEffect.TransactionSaved -> {
                    viewModel.reset()
                    onSaved()
                }
                is TransactionFormEffect.ShowError -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        MoolSectionHeader(title = "Add Transaction")
        Spacer(Modifier.height(20.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Transaction Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                TypeSelector(
                    selected = state.type,
                    onSelect = { viewModel.accept(TransactionFormIntent.SetType(it)) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = {
                        viewModel.accept(TransactionFormIntent.SetAmount(it))
                        viewModel.accept(TransactionFormIntent.DismissError)
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.currency,
                        onValueChange = { viewModel.accept(TransactionFormIntent.SetCurrency(it.uppercase())) },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                    OutlinedTextField(
                        value = state.category,
                        onValueChange = { viewModel.accept(TransactionFormIntent.SetCategory(it)) },
                        label = { Text("Category") },
                        placeholder = { Text("e.g. Food, Transport") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.accept(TransactionFormIntent.SetDescription(it)) },
                    label = { Text("Description") },
                    placeholder = { Text("Optional description") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        ErrorBanner(state.error, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { viewModel.accept(TransactionFormIntent.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    "Save Transaction",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selected
            val containerColor by animateColorAsState(
                targetValue = when {
                    isSelected && type == TransactionType.INCOME -> MaterialTheme.colorScheme.tertiary
                    isSelected && type == TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "typeColor",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                label = "typeTextColor",
            )

            if (isSelected) {
                FilledTonalButton(
                    onClick = { onSelect(type) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = containerColor,
                        contentColor = textColor,
                    ),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        type.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = { onSelect(type) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        type.name,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
