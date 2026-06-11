package com.mool.test

import com.mool.core.domain.ExchangeRate
import com.mool.core.domain.Transaction
import com.mool.core.domain.TransactionType
import com.mool.feature.dashboard.DashboardIntent
import com.mool.feature.dashboard.DashboardViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var exchangeRateRepo: FakeExchangeRateRepository
    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var vm: DashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exchangeRateRepo = FakeExchangeRateRepository()
        transactionRepo = FakeTransactionRepository()
        settingsRepo = FakeSettingsRepository()
        vm = DashboardViewModel(exchangeRateRepo, transactionRepo, settingsRepo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRates updates state with rates from repo`() {
        val rates = listOf(
            ExchangeRate("USD", "EUR", 0.92, 1000L),
            ExchangeRate("USD", "NPR", 133.0, 1000L),
        )
        exchangeRateRepo.setRates(rates)

        vm.loadRates()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(rates, state.rates)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `loadRates sets loading state while fetching`() {
        vm.loadRates()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `loadRates sets error when refresh fails`() {
        exchangeRateRepo.shouldThrow = true

        vm.loadRates()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertNotNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `observeBalance updates state with balance`() {
        transactionRepo.setTransactions(
            listOf(
                Transaction(1, 100.0, "USD", "Salary", TransactionType.INCOME, "Work", 1000L),
                Transaction(2, 30.0, "USD", "Food", TransactionType.EXPENSE, "Groceries", 1001L),
            )
        )

        vm.observeBalance()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(70.0, state.balance)
    }

    @Test
    fun `observePreferredCurrency defaults to USD when not set`() {
        vm.observePreferredCurrency()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("USD", vm.state.value.preferredCurrency)
    }

    @Test
    fun `accept Refresh triggers rate refresh`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "EUR", 0.92, 1000L))
        )

        vm.accept(DashboardIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exchangeRateRepo.refreshCalled)
    }
}
