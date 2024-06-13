package com.example.composechatsample.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
public data class StreamTypography(
    public val title1: TextStyle,
    public val title3: TextStyle,
    public val title3Bold: TextStyle,
    public val body: TextStyle,
    public val bodyItalic: TextStyle,
    public val bodyBold: TextStyle,
    public val footnote: TextStyle,
    public val footnoteItalic: TextStyle,
    public val footnoteBold: TextStyle,
    public val captionBold: TextStyle,
    public val tabBar: TextStyle,
    public val singleEmoji: TextStyle,
    public val emojiOnly: TextStyle,
) {

    public companion object {
        public fun defaultTypography(fontFamily: FontFamily? = null): StreamTypography = StreamTypography(
            title1 = TextStyle(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            title3 = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            title3Bold = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            body = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            bodyItalic = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily,
            ),
            bodyBold = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            footnote = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            footnoteItalic = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily,
            ),
            footnoteBold = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            captionBold = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W700,
                fontFamily = fontFamily,
            ),
            tabBar = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            singleEmoji = TextStyle(
                fontSize = 50.sp,
                fontFamily = fontFamily,
            ),
            emojiOnly = TextStyle(
                fontSize = 50.sp,
                fontFamily = fontFamily,
            ),
        )
    }
}