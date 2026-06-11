package com.mool.test

import com.mool.core.domain.SettingsKeys
import com.mool.feature.settings.SettingsIntent
import com.mool.feature.settings.SettingsViewModel
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var vm: SettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepo = FakeSettingsRepository()
        vm = SettingsViewModel(settingsRepo, isBiometricSupported = true)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = vm.state.value
        assertEquals("USD", state.preferredCurrency)
        assertEquals(false, state.biometricEnabled)
        assertEquals(true, state.isLoading)
    }

    @Test
    fun `LoadSettings observes preferred currency`() {
        vm.accept(SettingsIntent.LoadSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.state.value.isLoading)

        kotlinx.coroutines.runBlocking {
            settingsRepo.setSetting(SettingsKeys.PREFERRED_CURRENCY, "EUR")
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("EUR", vm.state.value.preferredCurrency)
    }

    @Test
    fun `SetCurrency persists to repository`() {
        vm.accept(SettingsIntent.SetCurrency("GBP"))
        testDispatcher.scheduler.advanceUntilIdle()

        kotlinx.coroutines.runBlocking {
            assertEquals("GBP", settingsRepo.getSetting(SettingsKeys.PREFERRED_CURRENCY))
        }
    }

    @Test
    fun `SetBiometricLock persists to repository`() {
        vm.accept(SettingsIntent.SetBiometricLock(true))
        testDispatcher.scheduler.advanceUntilIdle()

        kotlinx.coroutines.runBlocking {
            assertEquals("true", settingsRepo.getSetting(SettingsKeys.BIOMETRIC_ENABLED))
        }
    }

    @Test
    fun `SetBiometricLock toggles off`() {
        vm.accept(SettingsIntent.SetBiometricLock(true))
        testDispatcher.scheduler.advanceUntilIdle()

        vm.accept(SettingsIntent.SetBiometricLock(false))
        testDispatcher.scheduler.advanceUntilIdle()

        kotlinx.coroutines.runBlocking {
            assertEquals("false", settingsRepo.getSetting(SettingsKeys.BIOMETRIC_ENABLED))
        }
    }

    @Test
    fun `isBiometricAvailable is set from constructor`() {
        val vmWithoutBio = SettingsViewModel(settingsRepo, isBiometricSupported = false)
        vmWithoutBio.accept(SettingsIntent.LoadSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vmWithoutBio.state.value.isBiometricAvailable)

        val vmWithBio = SettingsViewModel(settingsRepo, isBiometricSupported = true)
        vmWithBio.accept(SettingsIntent.LoadSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vmWithBio.state.value.isBiometricAvailable)
    }

    @Test
    fun `LoadSettings observes biometric enabled state`() {
        kotlinx.coroutines.runBlocking {
            settingsRepo.setSetting(SettingsKeys.BIOMETRIC_ENABLED, "true")
        }
        vm.accept(SettingsIntent.LoadSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.biometricEnabled)
    }

    @Test
    fun `preferred currency defaults to USD when not set`() {
        vm.accept(SettingsIntent.LoadSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("USD", vm.state.value.preferredCurrency)
    }
}
