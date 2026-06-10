package com.mool.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mool.App
import com.mool.core.database.DatabaseDriverFactory
import com.mool.core.ui.MoolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MoolTheme {
                App(DatabaseDriverFactory(this))
            }
        }
    }
}
