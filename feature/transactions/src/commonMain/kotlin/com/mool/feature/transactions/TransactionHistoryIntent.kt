package com.mool.feature.transactions

sealed interface TransactionHistoryIntent {
    data object Refresh : TransactionHistoryIntent
    data class DeleteTransaction(val id: Long) : TransactionHistoryIntent
}
