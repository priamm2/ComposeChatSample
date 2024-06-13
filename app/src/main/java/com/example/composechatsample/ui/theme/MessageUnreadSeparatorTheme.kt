package com.example.composechatsample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Immutable
public data class MessageUnreadSeparatorTheme(
    public val textStyle: TextStyle,
    public val backgroundColor: Color,
) {

    public companion object {

        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageUnreadSeparatorTheme {
            return MessageUnreadSeparatorTheme(
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasisInverse,
                ),
                backgroundColor = colors.overlayDark,
            )
        }
    }
}