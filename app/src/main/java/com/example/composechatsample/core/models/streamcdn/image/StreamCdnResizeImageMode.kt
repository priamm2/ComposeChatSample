package com.example.composechatsample.core.models.streamcdn.image

public enum class StreamCdnResizeImageMode(public val queryParameterName: String) {
    CLIP("clip"),
    CROP("crop"),
    FILL("fill"),
    SCALE("scale"),
}
