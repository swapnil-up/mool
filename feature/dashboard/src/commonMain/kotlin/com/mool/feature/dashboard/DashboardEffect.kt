package com.mool.feature.dashboard

sealed interface DashboardEffect {
    data class ShowError(val message: String) : DashboardEffect
}
