package com.example.composechatsample.common

import androidx.annotation.FloatRange
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnCropImageMode
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnResizeImageMode

public data class StreamCdnImageResizing(
    val imageResizingEnabled: Boolean,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) val resizedWidthPercentage: Float,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) val resizedHeightPercentage: Float,
    val resizeMode: StreamCdnResizeImageMode?,
    val cropMode: StreamCdnCropImageMode?,
) {

    public companion object {

        public fun defaultStreamCdnImageResizing(): StreamCdnImageResizing = StreamCdnImageResizing(
            imageResizingEnabled = false,
            resizedWidthPercentage = 1f,
            resizedHeightPercentage = 1f,
            resizeMode = null,
            cropMode = null,
        )
    }
}