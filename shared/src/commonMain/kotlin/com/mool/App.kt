package com.mool

import androidx.compose.runtime.Composable
import com.mool.core.ui.MoolTheme
import com.mool.feature.dashboard.DashboardScreen

@Composable
fun App() {
    MoolTheme {
        DashboardScreen()
    }
}
