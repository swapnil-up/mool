package com.mool.core.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Suppress("DEPRECATION")
@Composable
actual fun BiometricLock(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    var unlocked by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(enabled) {
        unlocked = !enabled
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            unlocked = true
        }
    }

    if (!unlocked) {
        LaunchedEffect(Unit) {
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val intent = km.createConfirmDeviceCredentialIntent(
                "Unlock Mool",
                "Verify your identity to access the app"
            )
            if (intent != null) {
                launcher.launch(intent)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Mool",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        "Device credential required to access the app",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = {
                        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        val intent = km.createConfirmDeviceCredentialIntent(
                            "Unlock Mool",
                            "Verify your identity to access the app"
                        )
                        if (intent != null) {
                            launcher.launch(intent)
                        }
                    }) {
                        Text("Unlock")
                    }
                }
            }
        }

        return
    }

    content()
}

@Composable
actual fun isBiometricAuthenticationAvailable(): Boolean {
    val context = LocalContext.current
    val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return km.isDeviceSecure
}
