package com.mool.core.security

import androidx.compose.runtime.Composable

@Composable
actual fun BiometricLock(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    content()
}

@Composable
actual fun isBiometricAuthenticationAvailable(): Boolean = false
