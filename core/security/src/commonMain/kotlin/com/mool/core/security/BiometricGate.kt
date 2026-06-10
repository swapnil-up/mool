package com.mool.core.security

import kotlinx.coroutines.flow.Flow

sealed interface BiometricResult {
    data object Success : BiometricResult
    data class Error(val message: String) : BiometricResult
    data object NotAvailable : BiometricResult
}

interface BiometricGate {
    fun isAvailable(): Boolean
    suspend fun authenticate(reason: String): BiometricResult
}
