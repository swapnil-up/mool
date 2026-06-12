package com.mool.feature.transactions

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolEmptyState
import com.mool.core.ui.MoolShimmerCard
import com.mool.core.ui.toFixed
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
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

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.accept(TransactionHistoryIntent.Refresh) },
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Transaction History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
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
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filterType == null,
                        onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(null)) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                    FilterChip(
                        selected = state.filterType == TransactionType.INCOME,
                        onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(TransactionType.INCOME)) },
                        label = { Text("Income") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    )
                    FilterChip(
                        selected = state.filterType == TransactionType.EXPENSE,
                        onClick = { viewModel.accept(TransactionHistoryIntent.SetFilterType(TransactionType.EXPENSE)) },
                        label = { Text("Expense") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                ErrorBanner(state.error, modifier = Modifier.fillMaxWidth())
            }

            when {
                state.isLoading && state.transactions.isEmpty() -> {
                    items(5) { MoolShimmerCard(Modifier.padding(vertical = 4.dp)) }
                }
                state.transactions.isEmpty() -> {
                    item {
                        val msg = if (state.searchQuery.isNotEmpty() || state.filterType != null)
                            "No matching transactions"
                        else
                            "No transactions yet"
                        val subtitle = if (state.searchQuery.isNotEmpty() || state.filterType != null)
                            "Try a different search or filter"
                        else
                            "Add your first transaction to get started"
                        MoolEmptyState(
                            title = msg,
                            subtitle = subtitle,
                            action = if (state.searchQuery.isEmpty() && state.filterType == null) null
                            else {
                                {
                                    Button(
                                        onClick = {
                                            viewModel.accept(TransactionHistoryIntent.SetSearchQuery(""))
                                            viewModel.accept(TransactionHistoryIntent.SetFilterType(null))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    ) { Text("Clear filters") }
                                }
                            },
                        )
                    }
                }
                else -> {
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
}

@Composable
private fun TransactionCard(transaction: Transaction, onDelete: () -> Unit) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val prefix = if (isIncome) "+" else "-"
    val bgColor = if (isIncome) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)

    MoolCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { transaction.category.ifEmpty { transaction.type.name } },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "✕",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
