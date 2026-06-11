package com.mool.feature.transactions

import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType

data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
