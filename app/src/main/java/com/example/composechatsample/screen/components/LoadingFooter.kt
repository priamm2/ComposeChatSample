package com.example.composechatsample.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun LoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = ChatTheme.colors.appBackground)
            .padding(top = 8.dp, bottom = 48.dp),
    ) {
        LoadingIndicator(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center),
        )
    }
}