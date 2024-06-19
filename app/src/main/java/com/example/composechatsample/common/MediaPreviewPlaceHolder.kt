package com.example.composechatsample.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
internal fun MediaPreviewPlaceHolder(
    asyncImagePainterState: AsyncImagePainter.State,
    isImage: Boolean = false,
    progressIndicatorStrokeWidth: Dp,
    progressIndicatorFillMaxSizePercentage: Float,
    placeholderIconTintColor: Color = ChatTheme.colors.textLowEmphasis,

    ) {
    val painter = painterResource(
        id = R.drawable.stream_compose_ic_image_picker,
    )

    val imageModifier = Modifier.fillMaxSize(0.4f)

    when {
        asyncImagePainterState is AsyncImagePainter.State.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .fillMaxSize(progressIndicatorFillMaxSizePercentage),
                strokeWidth = progressIndicatorStrokeWidth,
                color = ChatTheme.colors.primaryAccent,
            )
        }
        asyncImagePainterState is AsyncImagePainter.State.Error && isImage -> Icon(
            tint = placeholderIconTintColor,
            modifier = imageModifier,
            painter = painter,
            contentDescription = null,
        )
        asyncImagePainterState is AsyncImagePainter.State.Success -> {}
        asyncImagePainterState is AsyncImagePainter.State.Empty && isImage -> {
            Icon(
                tint = placeholderIconTintColor,
                modifier = imageModifier,
                painter = painter,
                contentDescription = null,
            )
        }
    }
}