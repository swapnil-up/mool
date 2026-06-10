package com.mool.feature.transactions

import com.mool.core.domain.Transaction

data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
