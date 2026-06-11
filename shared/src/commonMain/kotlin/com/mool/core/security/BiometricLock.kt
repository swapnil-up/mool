package com.mool.core.security

import androidx.compose.runtime.Composable

@Composable
expect fun BiometricLock(
    enabled: Boolean,
    content: @Composable () -> Unit,
)

@Composable
expect fun isBiometricAuthenticationAvailable(): Boolean
