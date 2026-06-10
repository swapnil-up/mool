package com.mool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mool.core.database.DatabaseDriverFactory
import com.mool.core.database.ExchangeRateRepositoryImpl
import com.mool.core.database.MoolDatabase
import com.mool.core.database.SettingsRepositoryImpl
import com.mool.core.database.TransactionRepositoryImpl
import com.mool.core.network.FxApiClient
import com.mool.core.network.MoolHttpClient
import com.mool.core.ui.MoolTheme
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
    val exchangeRepo = remember { ExchangeRateRepositoryImpl(fxApiClient, database) }
    val transactionRepo = remember { TransactionRepositoryImpl(database) }
    val settingsRepo = remember { SettingsRepositoryImpl(database) }
    val dashboardVm = remember { DashboardViewModel(exchangeRepo, transactionRepo, settingsRepo) }
    val transactionVm = remember { TransactionFormViewModel(transactionRepo) }
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

@Composable
private fun DashboardIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val s = size.width / 3
        drawRect(Color.Gray, size = androidx.compose.ui.geometry.Size(s, s))
        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(s * 2, 0f), size = androidx.compose.ui.geometry.Size(s, s))
        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(0f, s * 2), size = androidx.compose.ui.geometry.Size(s, s))
        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(s * 2, s * 2), size = androidx.compose.ui.geometry.Size(s, s))
    }
}

@Composable
private fun AddIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val w = size.width / 6
        val h = size.width / 6
        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(cx - w / 2, cy - h * 2.5f), size = androidx.compose.ui.geometry.Size(w, h * 5))
        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(cx - h * 2.5f, cy - w / 2), size = androidx.compose.ui.geometry.Size(h * 5, w))
    }
}

@Composable
private fun HistoryIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width * 0.75f
        val h = size.height / 8
        val y = size.height * 0.25f
        drawRoundRect(Color.Gray, size = androidx.compose.ui.geometry.Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
        drawRoundRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(0f, y + h * 2), size = androidx.compose.ui.geometry.Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
        drawRoundRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(0f, (y + h * 2) * 2), size = androidx.compose.ui.geometry.Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
    }
}

@Composable
private fun SettingsIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val r = size.width * 0.3f
        drawCircle(Color.Gray, radius = r, center = c, style = Stroke(width = 2f))
    }
}

