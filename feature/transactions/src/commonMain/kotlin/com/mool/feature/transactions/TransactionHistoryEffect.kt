package com.mool.feature.transactions

sealed interface TransactionHistoryEffect {
    data class ShowError(val message: String) : TransactionHistoryEffect
}
