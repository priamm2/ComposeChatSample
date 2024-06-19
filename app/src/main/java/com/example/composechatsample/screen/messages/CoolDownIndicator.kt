package com.example.composechatsample.screen.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun CoolDownIndicator(
    coolDownTime: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .padding(12.dp)
            .background(shape = RoundedCornerShape(24.dp), color = ChatTheme.colors.disabled),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = coolDownTime.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.bodyBold,
        )
    }
}