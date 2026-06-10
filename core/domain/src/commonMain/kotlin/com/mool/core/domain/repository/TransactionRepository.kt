package com.mool.core.domain.repository

import com.mool.core.domain.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
}
