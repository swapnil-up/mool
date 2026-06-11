package com.mool.test

import com.mool.core.domain.TransactionType
import com.mool.feature.transactions.TransactionFormIntent
import com.mool.feature.transactions.TransactionFormViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var txRepo: FakeTransactionRepository
    private lateinit var clock: FakeClock
    private lateinit var vm: TransactionFormViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        txRepo = FakeTransactionRepository()
        clock = FakeClock(time = 5000L)
        vm = TransactionFormViewModel(txRepo, clock)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = vm.state.value
        assertEquals("", state.amount)
        assertEquals("USD", state.currency)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals(false, state.isSubmitting)
        assertNull(state.error)
    }

    @Test
    fun `accept SetAmount updates amount`() {
        vm.accept(TransactionFormIntent.SetAmount("150.00"))
        assertEquals("150.00", vm.state.value.amount)
    }

    @Test
    fun `accept SetCurrency updates currency`() {
        vm.accept(TransactionFormIntent.SetCurrency("EUR"))
        assertEquals("EUR", vm.state.value.currency)
    }

    @Test
    fun `accept SetDescription updates description`() {
        vm.accept(TransactionFormIntent.SetDescription("Lunch"))
        assertEquals("Lunch", vm.state.value.description)
    }

    @Test
    fun `accept SetType updates transaction type`() {
        vm.accept(TransactionFormIntent.SetType(TransactionType.INCOME))
        assertEquals(TransactionType.INCOME, vm.state.value.type)
    }

    @Test
    fun `accept SetCategory updates category`() {
        vm.accept(TransactionFormIntent.SetCategory("Food & Dining"))
        assertEquals("Food & Dining", vm.state.value.category)
    }

    @Test
    fun `submit with valid amount saves transaction`() {
        vm.accept(TransactionFormIntent.SetAmount("250.00"))
        vm.accept(TransactionFormIntent.SetCurrency("USD"))
        vm.accept(TransactionFormIntent.SetDescription("Freelance payment"))
        vm.accept(TransactionFormIntent.SetType(TransactionType.INCOME))
        vm.accept(TransactionFormIntent.SetCategory("Work"))
        vm.accept(TransactionFormIntent.Submit)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(false, state.isSubmitting)
        assertNull(state.error)

        assertEquals(1, txRepo.addedTransactions.size)
        val saved = txRepo.addedTransactions.first()
        assertEquals(250.0, saved.amount)
        assertEquals("USD", saved.currency)
        assertEquals("Freelance payment", saved.description)
        assertEquals(TransactionType.INCOME, saved.type)
        assertEquals("Work", saved.category)
        assertEquals(5000L, saved.timestamp)
    }

    @Test
    fun `submit with empty amount shows validation error`() {
        vm.accept(TransactionFormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(false, state.isSubmitting)
        assertNotNull(state.error)
        assertEquals(0, txRepo.addedTransactions.size)
    }

    @Test
    fun `submit with zero amount shows validation error`() {
        vm.accept(TransactionFormIntent.SetAmount("0"))
        vm.accept(TransactionFormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertNotNull(state.error)
        assertEquals(0, txRepo.addedTransactions.size)
    }

    @Test
    fun `submit with negative amount shows validation error`() {
        vm.accept(TransactionFormIntent.SetAmount("-50"))
        vm.accept(TransactionFormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertNotNull(state.error)
        assertEquals(0, txRepo.addedTransactions.size)
    }

    @Test
    fun `submit when repo throws shows error`() {
        txRepo.shouldThrow = true
        vm.accept(TransactionFormIntent.SetAmount("100"))
        vm.accept(TransactionFormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(false, state.isSubmitting)
        assertNotNull(state.error)
    }

    @Test
    fun `DismissError clears error`() {
        txRepo.shouldThrow = true
        vm.accept(TransactionFormIntent.SetAmount("100"))
        vm.accept(TransactionFormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        vm.accept(TransactionFormIntent.DismissError)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `reset clears form state`() {
        vm.accept(TransactionFormIntent.SetAmount("100"))
        vm.accept(TransactionFormIntent.SetDescription("Test"))
        vm.accept(TransactionFormIntent.SetCurrency("EUR"))
        vm.reset()

        val state = vm.state.value
        assertEquals("", state.amount)
        assertEquals("USD", state.currency)
        assertEquals("", state.description)
        assertEquals(TransactionType.EXPENSE, state.type)
    }
}
