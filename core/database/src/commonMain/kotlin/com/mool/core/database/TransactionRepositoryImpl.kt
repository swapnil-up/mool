package com.mool.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.core.domain.repository.TransactionRepository
import com.mool.core.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    db: MoolDatabase,
    private val encryptionManager: EncryptionManager,
) : TransactionRepository {

    private val queries = db.transactionQueries

    override fun observeTransactions(): Flow<List<Transaction>> {
        return queries.getAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeBalance(): Flow<Double> {
        return queries.getBalance()
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.SUM ?: 0.0 }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        withContext(Dispatchers.Default) {
            queries.insert(
                amount = transaction.amount,
                currency = transaction.currency,
                description = encrypt(transaction.description),
                type = transaction.type.name,
                category = encrypt(transaction.category),
                created_at = transaction.timestamp,
            )
        }
    }

    override suspend fun deleteTransaction(id: Long) {
        withContext(Dispatchers.Default) {
            queries.deleteById(id)
        }
    }

    private fun Transactions.toDomain() = Transaction(
        id = id,
        amount = amount,
        currency = currency,
        description = try { decrypt(description) } catch (_: Exception) { description },
        type = TransactionType.valueOf(type),
        category = try { decrypt(category) } catch (_: Exception) { category },
        timestamp = created_at,
    )

    private fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        val ciphertext = encryptionManager.encrypt(plaintext.encodeToByteArray())
        return ciphertext.joinToString("") { b ->
            b.toInt().and(0xFF).toString(16).padStart(2, '0')
        }
    }

    private fun decrypt(hex: String): String {
        if (hex.isEmpty()) return ""
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return encryptionManager.decrypt(bytes).decodeToString()
    }
}
