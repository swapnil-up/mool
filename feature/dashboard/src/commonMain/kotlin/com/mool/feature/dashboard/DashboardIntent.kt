package com.mool.feature.dashboard

sealed interface DashboardIntent {
    data object Refresh : DashboardIntent
}
