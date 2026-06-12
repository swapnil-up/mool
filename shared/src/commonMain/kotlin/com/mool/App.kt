package com.mool

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.mool.core.ui.ThemeMode
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

private data class Tab(val label: String, val icon: @Composable (Boolean) -> Unit)

private val tabs = listOf(
    Tab("Dashboard") { selected -> DashboardIcon(tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
    Tab("Add") { selected -> AddIcon(tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
    Tab("History") { selected -> HistoryIcon(tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
    Tab("Remittance") { selected -> RemittanceIcon(tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
    Tab("Settings") { selected -> SettingsIcon(tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
)

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

    val dashboardVm = remember { DashboardViewModel(exchangeRateRepo, transactionRepo, settingsRepo) }
    val transactionVm = remember { TransactionFormViewModel(transactionRepo, clock) }
    val historyVm = remember { TransactionHistoryViewModel(transactionRepo) }
    val remittanceVm = remember { RemittanceViewModel(exchangeRateRepo) }

    val biometricAvailable = isBiometricAuthenticationAvailable()
    val settingsVm = remember { SettingsViewModel(settingsRepo, biometricAvailable) }
    val settingsState by settingsVm.state.collectAsState()

    var biometricEnabled by remember { mutableStateOf(false) }
    var biometricLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val value = settingsRepo.getSetting(SettingsKeys.BIOMETRIC_ENABLED)
        biometricEnabled = value == "true"
        biometricLoaded = true
    }

    val themeMode = settingsState.themeMode

    BiometricLock(enabled = biometricLoaded && biometricEnabled) {
        var selectedTab by remember { mutableStateOf(0) }
        val snackbarHostState = remember { SnackbarHostState() }

        MoolTheme(themeMode = if (biometricLoaded) themeMode else ThemeMode.FOLLOW_SYSTEM) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { tab.icon(selectedTab == index) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                },
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally { width -> direction * width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        },
                        label = "tabContent",
                    ) { index ->
                        when (index) {
                            0 -> DashboardScreen(viewModel = dashboardVm)
                            1 -> TransactionFormScreen(
                                viewModel = transactionVm,
                                onSaved = { selectedTab = 2 },
                            )
                            2 -> TransactionHistoryScreen(viewModel = historyVm)
                            3 -> RemittanceScreen(viewModel = remittanceVm)
                            4 -> SettingsScreen(viewModel = settingsVm)
                        }
                    }
                }
            }
        }
    }
}
