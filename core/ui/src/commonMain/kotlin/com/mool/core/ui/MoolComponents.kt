package com.mool.core.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ErrorBanner(error: String?, modifier: Modifier = Modifier) {
    if (error != null) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun DashboardIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = Icons.Outlined.Home,
        contentDescription = "Dashboard",
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun AddIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = Icons.Outlined.Add,
        contentDescription = "Add",
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun HistoryIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = Icons.Outlined.History,
        contentDescription = "History",
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun SettingsIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = Icons.Outlined.Settings,
        contentDescription = "Settings",
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun RemittanceIcon(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = Icons.Outlined.SwapHoriz,
        contentDescription = "Remittance",
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun MoolEmptyState(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

@Composable
fun MoolCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            content()
        }
    }
}

@Composable
fun MoolAmountText(
    amount: Double,
    currency: String = "",
    prefix: String = "",
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Text(
        text = "$prefix${amount.toFixed(2)}${if (currency.isNotEmpty()) " $currency" else ""}",
        style = style,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun MoolShimmerEffect(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )

    val shimmerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerColor),
    )
}

@Composable
fun MoolShimmerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MoolShimmerEffect(width = 80.dp, height = 12.dp)
            Spacer(Modifier.height(8.dp))
            MoolShimmerEffect(width = 160.dp, height = 20.dp)
            Spacer(Modifier.height(12.dp))
            MoolShimmerEffect(height = 12.dp)
            Spacer(Modifier.height(8.dp))
            MoolShimmerEffect(width = 120.dp, height = 12.dp)
        }
    }
}

@Composable
fun MoolSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        trailing?.invoke()
    }
}

@Composable
fun MoolDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}
