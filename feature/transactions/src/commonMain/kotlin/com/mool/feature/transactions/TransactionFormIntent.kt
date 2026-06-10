package com.mool.feature.transactions

import com.mool.core.domain.TransactionType

sealed interface TransactionFormIntent {
    data class SetAmount(val value: String) : TransactionFormIntent
    data class SetCurrency(val value: String) : TransactionFormIntent
    data class SetDescription(val value: String) : TransactionFormIntent
    data class SetType(val type: TransactionType) : TransactionFormIntent
    data class SetCategory(val value: String) : TransactionFormIntent
    data object Submit : TransactionFormIntent
    data object DismissError : TransactionFormIntent
}
