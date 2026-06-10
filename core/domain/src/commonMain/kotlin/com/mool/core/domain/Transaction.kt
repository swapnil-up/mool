package com.mool.core.domain

data class Transaction(
    val id: Long,
    val amount: Double,
    val currency: String,
    val description: String,
    val type: TransactionType,
    val category: String,
    val timestamp: Long,
)

enum class TransactionType { INCOME, EXPENSE }
