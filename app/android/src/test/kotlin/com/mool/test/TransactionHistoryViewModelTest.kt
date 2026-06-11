package com.mool.test

import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.feature.transactions.TransactionHistoryIntent
import com.mool.feature.transactions.TransactionHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var txRepo: FakeTransactionRepository
    private lateinit var vm: TransactionHistoryViewModel

    private val sampleTransactions = listOf(
        Transaction(1, 100.0, "USD", "Salary", TransactionType.INCOME, "Work", 1000L),
        Transaction(2, 25.0, "USD", "Groceries", TransactionType.EXPENSE, "Food", 1001L),
        Transaction(3, 50.0, "EUR", "Freelance", TransactionType.INCOME, "Work", 1002L),
        Transaction(4, 10.0, "USD", "Coffee", TransactionType.EXPENSE, "Food", 1003L),
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        txRepo = FakeTransactionRepository()
        vm = TransactionHistoryViewModel(txRepo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startObserving loads transactions into state`() {
        txRepo.setTransactions(sampleTransactions)

        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(4, state.transactions.size)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `startObserving sets loading state initially`() {
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, vm.state.value.isLoading)
    }

    @Test
    fun `search by description filters transactions`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetSearchQuery("coffee"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.transactions.size)
        assertEquals("Coffee", state.transactions.first().description)
    }

    @Test
    fun `search by category filters transactions`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetSearchQuery("food"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.state.value.transactions.size)
    }

    @Test
    fun `search by currency filters transactions`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetSearchQuery("eur"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.state.value.transactions.size)
        assertEquals("EUR", vm.state.value.transactions.first().currency)
    }

    @Test
    fun `search is case-insensitive`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetSearchQuery("SALARY"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.state.value.transactions.size)
    }

    @Test
    fun `filter by EXPENSE type`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetFilterType(TransactionType.EXPENSE))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(2, state.transactions.size)
        assertTrue(state.transactions.all { it.type == TransactionType.EXPENSE })
    }

    @Test
    fun `filter by INCOME type`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetFilterType(TransactionType.INCOME))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.state.value.transactions.size)
        assertTrue(vm.state.value.transactions.all { it.type == TransactionType.INCOME })
    }

    @Test
    fun `clear filter shows all transactions`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetFilterType(TransactionType.INCOME))
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetFilterType(null))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4, vm.state.value.transactions.size)
    }

    @Test
    fun `search combined with type filter`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.SetSearchQuery("work"))
        vm.accept(TransactionHistoryIntent.SetFilterType(TransactionType.INCOME))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.state.value.transactions.size)
    }

    @Test
    fun `delete removes transaction`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(TransactionHistoryIntent.DeleteTransaction(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(txRepo.deletedIds.contains(1L))
    }

    @Test
    fun `Refresh intent restarts observing`() {
        txRepo.setTransactions(sampleTransactions)
        vm.startObserving()
        testDispatcher.scheduler.advanceUntilIdle()

        val newTx = listOf(
            Transaction(5, 200.0, "USD", "Bonus", TransactionType.INCOME, "Work", 1004L)
        )
        txRepo.setTransactions(newTx)

        vm.accept(TransactionHistoryIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.state.value.transactions.size)
        assertEquals("Bonus", vm.state.value.transactions.first().description)
    }
}
