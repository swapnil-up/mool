package com.mool.feature.transactions

import com.mool.core.domain.TransactionType

data class TransactionFormState(
    val amount: String = "",
    val currency: String = "USD",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
