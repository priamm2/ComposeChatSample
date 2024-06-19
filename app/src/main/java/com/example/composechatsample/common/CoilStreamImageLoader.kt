package com.example.composechatsample.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import android.widget.ImageView
import coil.load
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.example.composechatsample.core.DispatcherProvider
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders

internal object CoilStreamImageLoader : StreamImageLoader {

    override var imageHeadersProvider: ImageHeadersProvider = DefaultImageHeadersProvider

    override suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: StreamImageLoader.ImageTransformation,
    ): Bitmap? = withContext(DispatcherProvider.IO) {
        url.takeUnless(String::isBlank)
            ?.let { url ->
                val imageResult = context.streamImageLoader.execute(
                    ImageRequest.Builder(context)
                        .headers(imageHeadersProvider.getImageRequestHeaders(url).toHeaders())
                        .data(url)
                        .applyTransformation(transformation)
                        .build(),
                )
                (imageResult.drawable as? BitmapDrawable)?.bitmap
            }
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.load(data, context.streamImageLoader) {
            data?.toString()?.let { url ->
                headers(imageHeadersProvider.getImageRequestHeaders(url).toHeaders())
            }

            if (placeholderResId != null) {
                placeholder(placeholderResId)
                fallback(placeholderResId)
                error(placeholderResId)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.load(data, context.streamImageLoader) {
            data?.toString()?.let { url ->
                headers(imageHeadersProvider.getImageRequestHeaders(url).toHeaders())
            }

            if (placeholderDrawable != null) {
                placeholder(placeholderDrawable)
                fallback(placeholderDrawable)
                error(placeholderDrawable)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }


    override suspend fun loadAndResize(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ) {
        val context = target.context

        val drawable = withContext(DispatcherProvider.IO) {
            val headersMap = data?.toString()?.let { url ->
                imageHeadersProvider.getImageRequestHeaders(url)
            } ?: emptyMap()
            val result = context.streamImageLoader.execute(
                ImageRequest.Builder(context)
                    .headers(headersMap.toHeaders())
                    .placeholder(placeholderDrawable)
                    .fallback(placeholderDrawable)
                    .error(placeholderDrawable)
                    .data(data)
                    .listener(
                        onStart = { onStart() },
                        onCancel = { onComplete() },
                        onError = { _, _ -> onComplete() },
                        onSuccess = { _, _ -> onComplete() },
                    )
                    .applyTransformation(transformation)
                    .build(),
            )

            result.drawable
        } ?: return

        if (drawable is ScaleDrawable &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable.child is AnimatedImageDrawable
        ) {
            (drawable.child as AnimatedImageDrawable).start()
        } else if (drawable is MovieDrawable) {
            drawable.start()
        }

        target.setImageDrawable(drawable)
    }

    override fun loadVideoThumbnail(
        target: ImageView,
        uri: Uri?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.load(uri, context.streamImageLoader) {
            uri?.toString()?.let { url ->
                headers(imageHeadersProvider.getImageRequestHeaders(url).toHeaders())
            }

            if (placeholderResId != null) {
                placeholder(placeholderResId)
                fallback(placeholderResId)
                error(placeholderResId)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }

    private fun ImageRequest.Builder.applyTransformation(
        transformation: StreamImageLoader.ImageTransformation,
    ): ImageRequest.Builder =
        when (transformation) {
            is StreamImageLoader.ImageTransformation.None -> this
            is StreamImageLoader.ImageTransformation.Circle -> transformations(
                CircleCropTransformation(),
            )
            is StreamImageLoader.ImageTransformation.RoundedCorners -> transformations(
                RoundedCornersTransformation(
                    transformation.radius,
                ),
            )
        }
}