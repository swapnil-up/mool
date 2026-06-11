package com.mool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.clock.Clock
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.domain.repository.TransactionRepository
import com.mool.core.security.BiometricLock
import com.mool.core.security.isBiometricAuthenticationAvailable
import com.mool.core.ui.AddIcon
import com.mool.core.ui.DashboardIcon
import com.mool.core.ui.HistoryIcon
import com.mool.core.ui.MoolTheme
import com.mool.core.ui.RemittanceIcon
import com.mool.core.ui.SettingsIcon
import com.mool.feature.dashboard.DashboardScreen
import com.mool.feature.dashboard.DashboardViewModel
import com.mool.feature.remittance.RemittanceScreen
import com.mool.feature.remittance.RemittanceViewModel
import com.mool.feature.settings.SettingsScreen
import com.mool.feature.settings.SettingsViewModel
import com.mool.feature.transactions.TransactionFormScreen
import com.mool.feature.transactions.TransactionFormViewModel
import com.mool.feature.transactions.TransactionHistoryScreen
import com.mool.feature.transactions.TransactionHistoryViewModel
import org.koin.compose.koinInject

private enum class Tab(val label: String) {
    DASHBOARD("Dashboard"),
    ADD("Add"),
    HISTORY("History"),
    REMITTANCE("Remittance"),
    SETTINGS("Settings"),
}

@Composable
fun App() {
    AppContent()
}

@Composable
private fun AppContent() {
    val exchangeRateRepo = koinInject<ExchangeRateRepository>()
    val transactionRepo = koinInject<TransactionRepository>()
    val settingsRepo = koinInject<SettingsRepository>()
    val clock = koinInject<Clock>()

    val dashboardVm = remember {
        DashboardViewModel(exchangeRateRepo, transactionRepo, settingsRepo)
    }
    val transactionVm = remember {
        TransactionFormViewModel(transactionRepo, clock)
    }
    val historyVm = remember {
        TransactionHistoryViewModel(transactionRepo)
    }
    val remittanceVm = remember {
        RemittanceViewModel(exchangeRateRepo)
    }

    val biometricAvailable = isBiometricAuthenticationAvailable()
    val settingsVm = remember {
        SettingsViewModel(settingsRepo, biometricAvailable)
    }

    var biometricEnabled by remember { mutableStateOf(false) }
    var biometricLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val value = settingsRepo.getSetting(SettingsKeys.BIOMETRIC_ENABLED)
        biometricEnabled = value == "true"
        biometricLoaded = true
    }

    BiometricLock(enabled = biometricLoaded && biometricEnabled) {
        var selectedTab by remember { mutableStateOf(Tab.DASHBOARD) }

        MoolTheme {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == Tab.DASHBOARD,
                            onClick = { selectedTab = Tab.DASHBOARD },
                            icon = { DashboardIcon() },
                            label = { Text(Tab.DASHBOARD.label) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == Tab.ADD,
                            onClick = { selectedTab = Tab.ADD },
                            icon = { AddIcon() },
                            label = { Text(Tab.ADD.label) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == Tab.HISTORY,
                            onClick = { selectedTab = Tab.HISTORY },
                            icon = { HistoryIcon() },
                            label = { Text(Tab.HISTORY.label) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == Tab.REMITTANCE,
                            onClick = { selectedTab = Tab.REMITTANCE },
                            icon = { RemittanceIcon() },
                            label = { Text(Tab.REMITTANCE.label) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == Tab.SETTINGS,
                            onClick = { selectedTab = Tab.SETTINGS },
                            icon = { SettingsIcon() },
                            label = { Text(Tab.SETTINGS.label) },
                        )
                    }
                },
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    when (selectedTab) {
                        Tab.DASHBOARD -> DashboardScreen(dashboardVm)
                        Tab.ADD -> TransactionFormScreen(transactionVm)
                        Tab.HISTORY -> TransactionHistoryScreen(historyVm)
                        Tab.REMITTANCE -> RemittanceScreen(remittanceVm)
                        Tab.SETTINGS -> SettingsScreen(settingsVm)
                    }
                }
            }
        }
    }
}
