package com.mool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mool.core.database.DatabaseDriverFactory
import com.mool.core.database.MoolDatabase
import com.mool.core.database.SettingsRepositoryImpl
import com.mool.core.database.SystemClock
import com.mool.core.database.TransactionRepositoryImpl
import com.mool.core.data.ExchangeRateRepositoryImpl
import com.mool.core.network.FxApiClient
import com.mool.core.network.MoolHttpClient
import com.mool.core.ui.AddIcon
import com.mool.core.ui.DashboardIcon
import com.mool.core.ui.HistoryIcon
import com.mool.core.ui.MoolTheme
import com.mool.core.ui.SettingsIcon
import com.mool.feature.dashboard.DashboardScreen
import com.mool.feature.dashboard.DashboardViewModel
import com.mool.feature.settings.SettingsScreen
import com.mool.feature.settings.SettingsViewModel
import com.mool.feature.transactions.TransactionFormScreen
import com.mool.feature.transactions.TransactionFormViewModel
import com.mool.feature.transactions.TransactionHistoryScreen
import com.mool.feature.transactions.TransactionHistoryViewModel

private enum class Tab(val label: String) {
    DASHBOARD("Dashboard"),
    ADD("Add"),
    HISTORY("History"),
    SETTINGS("Settings"),
}

@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    val httpClient = remember { MoolHttpClient.create() }
    val fxApiClient = remember { FxApiClient(httpClient) }
    val database = remember { MoolDatabase(databaseDriverFactory.createDriver()) }
    val clock = remember { SystemClock() }
    val exchangeRepo = remember { ExchangeRateRepositoryImpl(fxApiClient, database) }
    val transactionRepo = remember { TransactionRepositoryImpl(database) }
    val settingsRepo = remember { SettingsRepositoryImpl(database) }
    val dashboardVm = remember { DashboardViewModel(exchangeRepo, transactionRepo, settingsRepo) }
    val transactionVm = remember { TransactionFormViewModel(transactionRepo, clock) }
    val historyVm = remember { TransactionHistoryViewModel(transactionRepo) }
    val settingsVm = remember { SettingsViewModel(settingsRepo) }

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
                    Tab.SETTINGS -> SettingsScreen(settingsVm)
                }
            }
        }
    }
}
