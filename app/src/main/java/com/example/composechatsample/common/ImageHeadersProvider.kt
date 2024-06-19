package com.example.composechatsample.common

public interface ImageHeadersProvider {

    public fun getImageRequestHeaders(url: String): Map<String, String>
}

internal object DefaultImageHeadersProvider : ImageHeadersProvider {
    override fun getImageRequestHeaders(url: String): Map<String, String> = emptyMap<String, String>()
}