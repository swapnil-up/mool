package com.mool.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mool.core.ui.ErrorBanner
import com.mool.core.ui.MoolCard
import com.mool.core.ui.MoolDivider
import com.mool.core.ui.MoolSectionHeader
import com.mool.core.ui.ThemeMode
import kotlinx.coroutines.flow.collectLatest

private val CURRENCIES = listOf(
    "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD",
    "INR", "NPR", "BDT", "PKR", "LKR",
    "CNY", "HKD", "SGD", "MYR", "THB", "KRW",
    "BRL", "MXN", "ZAR", "TRY", "RUB",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.accept(SettingsIntent.LoadSettings)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowError -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        MoolSectionHeader(title = "Settings")
        Spacer(Modifier.height(20.dp))

        // Appearance section
        MoolSectionHeader(title = "Appearance")
        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose your preferred appearance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.accept(SettingsIntent.SetThemeMode(mode)) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        ) {
                            Text(
                                when (mode) {
                                    ThemeMode.FOLLOW_SYSTEM -> "System"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Preferences section
        MoolSectionHeader(title = "Preferences")
        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Preferred Currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your home currency for net balance display",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    CurrencyDropdown(
                        selected = state.preferredCurrency,
                        currencies = CURRENCIES,
                        onSelect = { viewModel.accept(SettingsIntent.SetCurrency(it)) },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Security section
        MoolSectionHeader(title = "Security")
        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Biometric Lock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (state.isBiometricAvailable) "Require biometrics to open the app"
                        else "Biometrics not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.biometricEnabled,
                    onCheckedChange = { viewModel.accept(SettingsIntent.SetBiometricLock(it)) },
                    enabled = state.isBiometricAvailable,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        MoolSectionHeader(title = "About")
        Spacer(Modifier.height(12.dp))

        MoolCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Mool",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Personal finance & remittance tracker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        "Version",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "0.2.0",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        ErrorBanner(state.error, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))

        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selected: String,
    currencies: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onSelect(currency)
                        expanded = false
                    },
                )
            }
        }
    }
}
