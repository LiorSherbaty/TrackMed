package com.trackmed.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.ui.theme.AfternoonColor
import com.trackmed.ui.theme.EveningColor
import com.trackmed.ui.theme.MorningColor
import com.trackmed.ui.theme.NightColor
import com.trackmed.ui.theme.NoonColor

@Composable
fun TimeWindowIcon(
    timeWindow: ETimeWindow,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val (icon, defaultTint) = when (timeWindow) {
        ETimeWindow.MORNING -> Icons.Default.WbSunny to MorningColor
        ETimeWindow.NOON -> Icons.Default.LightMode to NoonColor
        ETimeWindow.AFTERNOON -> Icons.Default.WbTwilight to AfternoonColor
        ETimeWindow.EVENING -> Icons.Default.Nightlight to EveningColor
        ETimeWindow.NIGHT -> Icons.Default.Bedtime to NightColor
    }

    Icon(
        imageVector = icon,
        contentDescription = timeWindow.name,
        modifier = modifier,
        tint = tint ?: defaultTint
    )
}
