package com.mool.feature.transactions

sealed interface TransactionFormEffect {
    data object TransactionSaved : TransactionFormEffect
    data class ShowError(val message: String) : TransactionFormEffect
}
