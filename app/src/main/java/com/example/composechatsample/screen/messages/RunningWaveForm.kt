package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.clipRect
import com.example.composechatsample.ui.theme.ChatTheme
import androidx.compose.ui.unit.Dp

@Composable
public fun RunningWaveForm(
    restartKey: Any,
    newValueKey: Int,
    latestValue: Int?,
    modifier: Modifier = Modifier,
    maxInputValue: Int = 20,
    barMinHeight: Float = 0.1f,
    barWidth: Dp = 8.dp,
    barGap: Dp = 2.dp,
    barCornerRadius: CornerRadius = CornerRadius(barWidth.value / 2.5f, barWidth.value / 2.5f),
    barBrush: Brush = Brush.linearGradient(
        Pair(0f, ChatTheme.colors.primaryAccent),
        Pair(1f, ChatTheme.colors.primaryAccent),
    ),
) {
    val waveformData = remember(restartKey) {
        mutableStateListOf<Int>()
    }

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    val maxBars by remember(canvasWidth) {
        derivedStateOf { (canvasWidth / (barWidth.value + barGap.value)).toInt() }
    }

    LaunchedEffect(newValueKey) {
        latestValue?.let {
            if (waveformData.count() <= maxBars) {
                waveformData.add(latestValue)
            } else {
                waveformData.removeFirst()
                waveformData.add(latestValue)
            }
            println(waveformData.toList())
        }
    }

    val minBarHeightFloat by remember(canvasHeight, barMinHeight) {
        derivedStateOf { canvasHeight * barMinHeight }
    }

    Canvas(modifier) {
        clipRect {
            canvasWidth = this.size.width
            canvasHeight = this.size.height

            canvasWidth = this.size.width
            canvasHeight = this.size.height

            waveformData.forEachIndexed { index, waveformItem ->
                val barHeight = (size.height * (waveformItem.toFloat() / maxInputValue))
                    .coerceIn(
                        minimumValue = minBarHeightFloat,
                        maximumValue = this.size.height,
                    )

                val xOffset = (barGap.value + barWidth.value) * index.toFloat()
                val yOffset = (this.size.height - barHeight) / 2

                this.drawRoundRect(
                    cornerRadius = barCornerRadius,
                    brush = barBrush,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(width = barWidth.value, height = barHeight),
                )
            }
        }
    }
}
