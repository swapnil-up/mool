package com.mool.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val chartColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFF3F51B5),
)

fun pickColor(index: Int) = chartColors[index % chartColors.size]

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color,
)

data class BarEntry(
    val label: String,
    val value: Float,
    val color: Color = pickColor(0),
)

@Composable
fun SpendingPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    centerLabel: String = "",
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f || slices.isEmpty()) return

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "pieChart",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        val strokeWidth = 48f
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val diameter = size.minDimension
                val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                val topLeft = Offset(
                    (size.width - arcSize.width) / 2f,
                    (size.height - arcSize.height) / 2f,
                )
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = (slice.value / total) * 360f * animationProgress
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        slices.forEachIndexed { index, slice ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = slice.color)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                val pct = (slice.value / total * 100).toInt()
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SpendingBarChart(
    bars: List<BarEntry>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
) {
    if (bars.isEmpty()) return

    val effectiveMax = maxValue ?: bars.maxOf { it.value }
    if (effectiveMax <= 0f) return

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "barChart",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                val fraction = (bar.value / effectiveMax).coerceIn(0f, 1f)
                val barHeight = (fraction * 120f * animationProgress).coerceAtLeast(0f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp),
                ) {
                    Box(
                        modifier = Modifier.weight(1f).width(24.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = bar.color,
                                topLeft = Offset(0f, size.height - barHeight.dp.toPx()),
                                size = Size(size.width, barHeight.dp.toPx()),
                                cornerRadius = CornerRadius(4f, 4f),
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetRow(
    label: String,
    spent: Float,
    budget: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val fraction = (spent / budget).coerceIn(0f, 1f)
    val overBudget = spent > budget

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${spent.toDouble().toFixed(2)} / ${budget.toDouble().toFixed(2)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (overBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (overBudget) MaterialTheme.colorScheme.error else color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}
