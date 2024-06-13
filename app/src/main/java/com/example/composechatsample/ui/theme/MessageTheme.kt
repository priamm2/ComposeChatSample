package com.example.composechatsample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Immutable
data class MessageTheme(
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val quotedTextStyle: TextStyle,
    val quotedBackgroundColor: Color,
    val deletedBackgroundColor: Color,
) {
    companion object {


        @Composable
        fun defaultOwnTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageTheme = defaultTheme(own = true, typography = typography, colors = colors)

        @Composable
        public fun defaultOtherTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageTheme = defaultTheme(own = false, typography = typography, colors = colors)

        @Composable
        @Suppress("DEPRECATION_ERROR")
        private fun defaultTheme(
            own: Boolean,
            typography: StreamTypography,
            colors: StreamColors,
        ): MessageTheme {
            return MessageTheme(
                textStyle = typography.bodyBold.copy(
                    color = when (own) {
                        true -> colors.ownMessageText
                        else -> colors.otherMessageText
                    },
                ),
                backgroundColor = when (own) {
                    true -> colors.ownMessagesBackground
                    else -> colors.otherMessagesBackground
                },
                quotedTextStyle = typography.bodyBold.copy(
                    color = when (own) {
                        true -> colors.ownMessageQuotedText
                        else -> colors.otherMessageQuotedText
                    },
                ),
                quotedBackgroundColor = when (own) {
                    true -> colors.ownMessageQuotedBackground
                    else -> colors.otherMessageQuotedBackground
                },
                deletedBackgroundColor = colors.deletedMessagesBackground,
            )
        }
    }
}