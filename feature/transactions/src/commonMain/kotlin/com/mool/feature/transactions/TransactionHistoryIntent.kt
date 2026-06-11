package com.mool.feature.transactions

import com.mool.core.domain.TransactionType

sealed interface TransactionHistoryIntent {
    data object Refresh : TransactionHistoryIntent
    data class DeleteTransaction(val id: Long) : TransactionHistoryIntent
    data class SetSearchQuery(val query: String) : TransactionHistoryIntent
    data class SetFilterType(val type: TransactionType?) : TransactionHistoryIntent
}
