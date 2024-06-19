package com.example.composechatsample.screen.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun BackButton(
    painter: Painter,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onBackPressed,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
        )
    }
}