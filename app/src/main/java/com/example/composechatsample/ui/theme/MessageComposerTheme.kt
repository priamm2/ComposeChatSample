package com.example.composechatsample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R

public data class MessageComposerTheme(
    val attachmentCancelIcon: ComposerCancelIconStyle,
    val linkPreview: ComposerLinkPreviewTheme,
    val inputField: ComposerInputFieldTheme,
) {

    public companion object {


        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageComposerTheme {
            return MessageComposerTheme(
                attachmentCancelIcon = ComposerCancelIconStyle.defaultStyle(colors),
                linkPreview = ComposerLinkPreviewTheme.defaultTheme(typography, colors),
                inputField = ComposerInputFieldTheme.defaultTheme(typography, shapes, colors),
            )
        }
    }
}

data class ComposerCancelIconStyle(
    val backgroundShape: Shape,
    val backgroundColor: Color,
    val painter: Painter,
    val tint: Color,
) {
    companion object {

        @Composable
        fun defaultStyle(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerCancelIconStyle {
            return ComposerCancelIconStyle(
                backgroundShape = CircleShape,
                backgroundColor = colors.overlayDark,
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                tint = colors.appBackground,
            )
        }
    }
}

data class ComposerLinkPreviewTheme(
    val imageSize: ComponentSize,
    val imageShape: Shape,
    val imagePadding: Dp,
    val separatorSize: ComponentSize,
    val separatorMarginStart: Dp,
    val separatorMarginEnd: Dp,
    val title: TextComponentStyle,
    val titleToSubtitle: Dp,
    val subtitle: TextComponentStyle,
    val cancelIcon: ComposerCancelIconStyle,
) {
    companion object {

        @Composable
        fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerLinkPreviewTheme {
            return ComposerLinkPreviewTheme(
                imageSize = ComponentSize(width = 48.dp, height = 48.dp),
                imageShape = RectangleShape,
                imagePadding = 4.dp,
                separatorSize = ComponentSize(width = 2.dp, height = 48.dp),
                separatorMarginStart = 4.dp,
                separatorMarginEnd = 8.dp,
                title = TextComponentStyle(
                    color = colors.textHighEmphasis,
                    style = typography.bodyBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                ),
                titleToSubtitle = 4.dp,
                subtitle = TextComponentStyle(
                    color = colors.textHighEmphasis,
                    style = typography.footnote,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                ),
                cancelIcon = ComposerCancelIconStyle.defaultStyle(colors),
            )
        }
    }
}

data class ComposerInputFieldTheme(
    val borderShape: Shape,
    val backgroundColor: Color,
    val textStyle: TextStyle,
    val cursorBrushColor: Color,
) {

    companion object {
        @Composable
        fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerInputFieldTheme {
            return ComposerInputFieldTheme(
                borderShape = shapes.inputField,
                backgroundColor = colors.inputBackground,
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasis,
                    textDirection = TextDirection.Content,
                ),
                cursorBrushColor = colors.primaryAccent,
            )
        }
    }
}