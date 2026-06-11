package com.mool.test

import com.mool.core.domain.ExchangeRate
import com.mool.feature.remittance.RemittanceIntent
import com.mool.feature.remittance.RemittanceViewModel
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
class RemittanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var exchangeRateRepo: FakeExchangeRateRepository
    private lateinit var vm: RemittanceViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exchangeRateRepo = FakeExchangeRateRepository()
        vm = RemittanceViewModel(exchangeRateRepo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeRates populates available currencies`() {
        exchangeRateRepo.setRates(
            listOf(
                ExchangeRate("USD", "EUR", 0.92, 1000L),
                ExchangeRate("USD", "NPR", 133.0, 1000L),
                ExchangeRate("USD", "JPY", 149.5, 1000L),
            )
        )

        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(false, state.isRatesLoading)
        assertEquals(listOf("EUR", "JPY", "NPR"), state.availableCurrencies)
    }

    @Test
    fun `USD to NPR calculates correctly with no fees`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "NPR", 133.0, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("100"))
        vm.accept(RemittanceIntent.SetSendCurrency("USD"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("NPR"))
        vm.accept(RemittanceIntent.SetFeePercent("0"))
        vm.accept(RemittanceIntent.SetMarginPercent("0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(133.0, state.appliedRate)
        assertNotNull(state.calculatedReceiveAmount)
        assertEquals(100.0 * 133.0, state.calculatedReceiveAmount!!, 0.001)
    }

    @Test
    fun `USD to same currency gives rate of 1`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "EUR", 0.92, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("100"))
        vm.accept(RemittanceIntent.SetSendCurrency("USD"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("USD"))
        vm.accept(RemittanceIntent.SetFeePercent("0"))
        vm.accept(RemittanceIntent.SetMarginPercent("0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1.0, state.appliedRate)
        assertEquals(100.0, state.calculatedReceiveAmount!!, 0.001)
    }

    @Test
    fun `NPR to USD uses inverse rate`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "NPR", 133.0, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("133"))
        vm.accept(RemittanceIntent.SetSendCurrency("NPR"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("USD"))
        vm.accept(RemittanceIntent.SetFeePercent("0"))
        vm.accept(RemittanceIntent.SetMarginPercent("0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1.0 / 133.0, state.appliedRate!!, 0.0001)
        assertEquals(1.0, state.calculatedReceiveAmount!!, 0.1)
    }

    @Test
    fun `NPR to EUR uses cross rate`() {
        exchangeRateRepo.setRates(
            listOf(
                ExchangeRate("USD", "NPR", 133.0, 1000L),
                ExchangeRate("USD", "EUR", 0.92, 1000L),
            )
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("133"))
        vm.accept(RemittanceIntent.SetSendCurrency("NPR"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("EUR"))
        vm.accept(RemittanceIntent.SetFeePercent("0"))
        vm.accept(RemittanceIntent.SetMarginPercent("0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedCrossRate = 0.92 / 133.0
        val state = vm.state.value
        assertEquals(expectedCrossRate, state.appliedRate!!, 0.0001)
        assertEquals(133.0 * expectedCrossRate, state.calculatedReceiveAmount!!, 0.01)
    }

    @Test
    fun `fees are applied correctly`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "NPR", 133.0, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("100"))
        vm.accept(RemittanceIntent.SetSendCurrency("USD"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("NPR"))
        vm.accept(RemittanceIntent.SetFeePercent("2.0"))
        vm.accept(RemittanceIntent.SetMarginPercent("1.0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        val converted = 100.0 * 133.0
        assertEquals(converted * 2.0 / 100.0, state.calculatedFee!!, 0.001)
        assertEquals(converted * 1.0 / 100.0, state.calculatedMargin!!, 0.001)
        assertEquals(converted * (1.0 - 0.02 - 0.01), state.calculatedReceiveAmount!!, 0.001)
    }

    @Test
    fun `invalid send amount shows error`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "NPR", 133.0, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("abc"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.calculatedReceiveAmount)
    }

    @Test
    fun `zero send amount shows error`() {
        vm.accept(RemittanceIntent.SetSendAmount("0"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `rate unavailable shows error`() {
        exchangeRateRepo.setRates(
            listOf(ExchangeRate("USD", "NPR", 133.0, 1000L))
        )
        vm.observeRates()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(RemittanceIntent.SetSendAmount("100"))
        vm.accept(RemittanceIntent.SetSendCurrency("USD"))
        vm.accept(RemittanceIntent.SetReceiveCurrency("XYZ"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `DismissError clears error`() {
        vm.accept(RemittanceIntent.SetSendAmount("abc"))
        vm.accept(RemittanceIntent.Calculate)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        vm.accept(RemittanceIntent.DismissError)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `SetSendCurrency updates send currency`() {
        vm.accept(RemittanceIntent.SetSendCurrency("EUR"))
        assertEquals("EUR", vm.state.value.sendCurrency)
    }

    @Test
    fun `SetReceiveCurrency updates receive currency`() {
        vm.accept(RemittanceIntent.SetReceiveCurrency("JPY"))
        assertEquals("JPY", vm.state.value.receiveCurrency)
    }

    @Test
    fun `SetFeePercent updates fee percent`() {
        vm.accept(RemittanceIntent.SetFeePercent("3.5"))
        assertEquals("3.5", vm.state.value.feePercent)
    }

    @Test
    fun `SetMarginPercent updates margin percent`() {
        vm.accept(RemittanceIntent.SetMarginPercent("2.0"))
        assertEquals("2.0", vm.state.value.marginPercent)
    }
}
