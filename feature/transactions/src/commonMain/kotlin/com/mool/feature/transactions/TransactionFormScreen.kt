package com.mool.feature.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mool.core.domain.TransactionType
import com.mool.core.ui.ErrorBanner
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
        Text("Add Transaction", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        TypeSelector(
            selected = state.type,
            onSelect = { viewModel.accept(TransactionFormIntent.SetType(it)) },
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.amount,
            onValueChange = { viewModel.accept(TransactionFormIntent.SetAmount(it)) },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.currency,
                onValueChange = { viewModel.accept(TransactionFormIntent.SetCurrency(it)) },
                label = { Text("Currency") },
                singleLine = true,
                modifier = Modifier.width(120.dp),
            )
            OutlinedTextField(
                value = state.category,
                onValueChange = { viewModel.accept(TransactionFormIntent.SetCategory(it)) },
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.accept(TransactionFormIntent.SetDescription(it)) },
            label = { Text("Description") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        ErrorBanner(state.error, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

        Button(
            onClick = { viewModel.accept(TransactionFormIntent.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save Transaction")
            }
        }
    }
}

@Composable
private fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selected
            if (isSelected) {
                Button(
                    onClick = { onSelect(type) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == TransactionType.INCOME)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(type.name)
                }
            } else {
                OutlinedButton(onClick = { onSelect(type) }) {
                    Text(type.name)
                }
            }
        }
    }
}
