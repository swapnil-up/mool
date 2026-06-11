package com.mool.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ErrorBanner(error: String?, modifier: Modifier = Modifier) {
    if (error != null) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
fun DashboardIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val s = size.width / 3
        drawRect(Color.Gray, size = Size(s, s))
        drawRect(Color.Gray, topLeft = Offset(s * 2, 0f), size = Size(s, s))
        drawRect(Color.Gray, topLeft = Offset(0f, s * 2), size = Size(s, s))
        drawRect(Color.Gray, topLeft = Offset(s * 2, s * 2), size = Size(s, s))
    }
}

@Composable
fun AddIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val w = size.width / 6
        val h = size.width / 6
        drawRect(Color.Gray, topLeft = Offset(cx - w / 2, cy - h * 2.5f), size = Size(w, h * 5))
        drawRect(Color.Gray, topLeft = Offset(cx - h * 2.5f, cy - w / 2), size = Size(h * 5, w))
    }
}

@Composable
fun HistoryIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width * 0.75f
        val h = size.height / 8
        val y = size.height * 0.25f
        val r = CornerRadius(2f, 2f)
        drawRoundRect(Color.Gray, size = Size(w, h), cornerRadius = r)
        drawRoundRect(Color.Gray, topLeft = Offset(0f, y + h * 2), size = Size(w, h), cornerRadius = r)
        drawRoundRect(Color.Gray, topLeft = Offset(0f, (y + h * 2) * 2), size = Size(w, h), cornerRadius = r)
    }
}

@Composable
fun SettingsIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.width * 0.3f
        drawCircle(Color.Gray, radius = r, center = c, style = Stroke(width = 2f))
    }
}

@Composable
fun RemittanceIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val w = size.width
        val h = size.height
        drawLine(Color.Gray, Offset(w * 0.2f, h * 0.4f), Offset(w * 0.8f, h * 0.4f), strokeWidth = 2f)
        drawLine(Color.Gray, Offset(w * 0.8f, h * 0.4f), Offset(w * 0.65f, h * 0.2f), strokeWidth = 2f)
        drawLine(Color.Gray, Offset(w * 0.8f, h * 0.4f), Offset(w * 0.65f, h * 0.6f), strokeWidth = 2f)
        drawLine(Color.Gray, Offset(w * 0.2f, h * 0.6f), Offset(w * 0.8f, h * 0.6f), strokeWidth = 2f)
        drawLine(Color.Gray, Offset(w * 0.2f, h * 0.6f), Offset(w * 0.35f, h * 0.4f), strokeWidth = 2f)
        drawLine(Color.Gray, Offset(w * 0.2f, h * 0.6f), Offset(w * 0.35f, h * 0.8f), strokeWidth = 2f)
    }
}
