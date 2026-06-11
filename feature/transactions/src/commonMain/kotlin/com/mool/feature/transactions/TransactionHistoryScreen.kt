package com.mool.feature.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.toFixed
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TransactionHistoryScreen(viewModel: TransactionHistoryViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startObserving()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TransactionHistoryEffect.ShowError -> {}
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Transaction History", style = MaterialTheme.typography.headlineSmall)
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.accept(TransactionHistoryIntent.SetSearchQuery(it)) },
            placeholder = { Text("Search transactions...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.accept(TransactionHistoryIntent.SetSearchQuery(state.searchQuery))
            }),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.filterType == null,
                onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(null)) },
                label = { Text("All") },
            )
            FilterChip(
                selected = state.filterType == TransactionType.INCOME,
                onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(TransactionType.INCOME)) },
                label = { Text("Income") },
            )
            FilterChip(
                selected = state.filterType == TransactionType.EXPENSE,
                onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(TransactionType.EXPENSE)) },
                label = { Text("Expense") },
            )
        }

        Spacer(Modifier.height(8.dp))

        ErrorBanner(state.error, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val msg = if (state.searchQuery.isNotEmpty() || state.filterType != null)
                    "No matching transactions"
                else
                    "No transactions yet"
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn {
                items(state.transactions, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        onDelete = { viewModel.accept(TransactionHistoryIntent.DeleteTransaction(tx.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(transaction: Transaction, onDelete: () -> Unit) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val prefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { transaction.category.ifEmpty { transaction.type.name } },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = transaction.currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${transaction.amount.toFixed(2)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Text("X", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


