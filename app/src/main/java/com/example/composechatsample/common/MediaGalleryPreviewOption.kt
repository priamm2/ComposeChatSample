package com.example.composechatsample.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

internal data class MediaGalleryPreviewOption(
    internal val title: String,
    internal val titleColor: Color,
    internal val iconPainter: Painter,
    internal val iconColor: Color,
    internal val action: MediaGalleryPreviewAction,
    internal val isEnabled: Boolean,
)