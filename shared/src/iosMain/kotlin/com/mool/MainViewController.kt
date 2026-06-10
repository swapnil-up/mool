package com.mool

import androidx.compose.ui.window.ComposeUIViewController
import com.mool.core.database.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }
