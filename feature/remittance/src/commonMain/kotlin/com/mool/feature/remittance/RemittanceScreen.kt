package com.mool.feature.remittance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun RemittanceScreen() {
    Text(
        "Remittance",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp),
    )
}
