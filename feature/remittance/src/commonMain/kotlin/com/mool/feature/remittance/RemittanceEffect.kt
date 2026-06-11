package com.mool.feature.remittance

sealed interface RemittanceEffect {
    data class ShowError(val message: String) : RemittanceEffect
}
