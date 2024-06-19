package com.example.composechatsample.common

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory

public fun interface StreamCoilImageLoaderFactory {

    public fun imageLoader(context: Context): ImageLoader

    public companion object {
        public fun defaultFactory(): StreamCoilImageLoaderFactory {
            return DefaultStreamCoilImageLoaderFactory
        }
    }
}

internal object DefaultStreamCoilImageLoaderFactory : StreamCoilImageLoaderFactory {


    private var imageLoader: ImageLoader? = null

    private var imageLoaderFactory: ImageLoaderFactory? = null

    override fun imageLoader(context: Context): ImageLoader = imageLoader ?: newImageLoader(context)

    @Synchronized
    private fun newImageLoader(context: Context): ImageLoader {
        imageLoader?.let { return it }

        val imageLoaderFactory = imageLoaderFactory ?: newImageLoaderFactory(context)
        return imageLoaderFactory.newImageLoader().apply {
            imageLoader = this
        }
    }

    private fun newImageLoaderFactory(context: Context): ImageLoaderFactory {
        return StreamImageLoaderFactory(context).apply {
            imageLoaderFactory = this
        }
    }
}