package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
public fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        TypingIndicatorAnimatedDot(0 * DotAnimationDurationMillis)
        TypingIndicatorAnimatedDot(1 * DotAnimationDurationMillis)
        TypingIndicatorAnimatedDot(2 * DotAnimationDurationMillis)
    }
}