package com.mool.test

import com.mool.core.domain.ExchangeRate
import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.core.domain.clock.Clock
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExchangeRateRepository : ExchangeRateRepository {
    private val _rates = MutableStateFlow<List<ExchangeRate>>(emptyList())
    var refreshCalled = false
    var shouldThrow = false

    override fun observeRates(): Flow<List<ExchangeRate>> = _rates

    override suspend fun refreshRates() {
        if (shouldThrow) error("Refresh failed")
        refreshCalled = true
    }

    fun setRates(rates: List<ExchangeRate>) {
        _rates.value = rates
    }
}

class FakeTransactionRepository : TransactionRepository {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _balance = MutableStateFlow(0.0)
    val addedTransactions = mutableListOf<Transaction>()
    val deletedIds = mutableListOf<Long>()
    var shouldThrow = false

    override fun observeTransactions(): Flow<List<Transaction>> = _transactions

    override fun observeBalance(): Flow<Double> = _balance

    override suspend fun addTransaction(transaction: Transaction) {
        if (shouldThrow) error("Add failed")
        addedTransactions.add(transaction)
        _transactions.value = _transactions.value + transaction
        updateBalance()
    }

    override suspend fun deleteTransaction(id: Long) {
        if (shouldThrow) error("Delete failed")
        deletedIds.add(id)
    }

    fun setTransactions(transactions: List<Transaction>) {
        _transactions.value = transactions
        updateBalance()
    }

    private fun updateBalance() {
        _balance.value = _transactions.value.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }
    }
}

class FakeSettingsRepository : SettingsRepository {
    private val settings = mutableMapOf<String, MutableStateFlow<String?>>()

    override fun observeSetting(key: String): Flow<String?> =
        settings.getOrPut(key) { MutableStateFlow(null) }

    override suspend fun getSetting(key: String): String? =
        settings[key]?.value

    override suspend fun setSetting(key: String, value: String) {
        settings.getOrPut(key) { MutableStateFlow(null) }.value = value
    }
}

class FakeClock(private val time: Long = 1000000L) : Clock {
    override fun now(): Long = time
}
