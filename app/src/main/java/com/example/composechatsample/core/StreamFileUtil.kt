package com.example.composechatsample.core

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.composechatsample.common.StreamFileProvider
import com.example.composechatsample.core.models.Attachment
import java.io.File
import java.io.IOException

private const val DEFAULT_BITMAP_QUALITY = 90

public object StreamFileUtil {

    private fun getFileProviderAuthority(context: Context): String {
        val compName = ComponentName(context, StreamFileProvider::class.java.name)
        val providerInfo = context.packageManager.getProviderInfo(compName, 0)
        return providerInfo.authority
    }

    public fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    public fun writeImageToSharableFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
                "share_image_${System.currentTimeMillis()}.png",
            )
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_BITMAP_QUALITY, out)
                out.flush()
            }
            getUriForFile(context, file)
        } catch (_: IOException) {
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getOrCreateStreamCacheDir(
        context: Context,
    ): Result<File> {
        return try {
            val file = File(context.cacheDir, STREAM_CACHE_DIR_NAME).also { streamCacheDir ->
                streamCacheDir.mkdirs()
            }

            Result.Success(file)
        } catch (e: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not get or create the Stream cache directory",
                    cause = e,
                ),
            )
        }
    }

    internal fun createFileInCacheDir(context: Context, fileName: String): Result<File> =
        try {
            getOrCreateStreamCacheDir(context)
                .flatMap { Result.Success(File(it, fileName)) }
        } catch (e: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not get or create the file.",
                    cause = e,
                ),
            )
        }

    @Suppress("TooGenericExceptionCaught")
    public fun clearStreamCache(
        context: Context,
    ): Result<Unit> {
        return try {
            val directory = File(context.cacheDir, STREAM_CACHE_DIR_NAME)
            directory.deleteRecursively()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could clear the Stream cache directory",
                    cause = e,
                ),
            )
        }
    }


    @Suppress("TooGenericExceptionCaught")
    public fun getFileFromCache(
        context: Context,
        attachment: Attachment,
    ): Result<Uri> {
        return try {
            when (val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)) {
                is Result.Failure -> getOrCreateCacheDirResult
                is Result.Success -> {
                    val streamCacheDir = getOrCreateCacheDirResult.value

                    val attachmentHashCode = (attachment.url ?: attachment.assetUrl)?.hashCode()
                    val fileName = CACHED_FILE_PREFIX + attachmentHashCode.toString() + attachment.name

                    val file = File(streamCacheDir, fileName)

                    val isFileCached = file.exists() &&
                        attachmentHashCode != null &&
                        file.length() == attachment.fileSize.toLong()

                    if (isFileCached) {
                        Result.Success(getUriForFile(context, file))
                    } else {
                        Result.Failure(Error.GenericError(message = "No such file in cache."))
                    }
                }
            }
        } catch (e: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Cannot determine if the file has been cached.",
                    cause = e,
                ),
            )
        }
    }

    @Suppress("ReturnCount")
    public suspend fun writeFileToShareableFile(
        context: Context,
        attachment: Attachment,
    ): Result<Uri> {
        val runCatching = kotlin.runCatching {
            when (val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)) {
                is Result.Failure -> getOrCreateCacheDirResult
                is Result.Success -> {
                    val streamCacheDir = getOrCreateCacheDirResult.value

                    val attachmentHashCode = (attachment.url ?: attachment.assetUrl)?.hashCode()
                    val fileName = CACHED_FILE_PREFIX + attachmentHashCode.toString() + attachment.name

                    val file = File(streamCacheDir, fileName)

                    if (file.exists() &&
                        attachmentHashCode != null &&
                        file.length() == attachment.fileSize.toLong()
                    ) {
                        Result.Success(getUriForFile(context, file))
                    } else {
                        val fileUrl = attachment.assetUrl ?: attachment.url ?: return Result.Failure(
                            Error.GenericError(message = "File URL cannot be null."),
                        )

                        when (val response = ChatClient.instance().downloadFile(fileUrl).await()) {
                            is Result.Success -> {
                                response.value.byteStream().use { inputStream ->
                                    file.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }

                                Result.Success(getUriForFile(context, file))
                            }
                            is Result.Failure -> response
                        }
                    }
                }
            }
        }

        return runCatching.getOrNull() ?: createFailureResultFromException(runCatching.exceptionOrNull())
    }

    private fun createFailureResultFromException(throwable: Throwable?): Result.Failure {
        return Result.Failure(
            throwable?.let { exception ->
                Error.ThrowableError(message = "Could not write to file.", cause = exception)
            } ?: Error.GenericError(message = "Could not write to file."),
        )
    }

    /**
     * The name of the Stream cache directory.
     *
     * This does not include file separators so do not forget to include them
     * when using this to access the directory or files contained within.
     */
    private const val STREAM_CACHE_DIR_NAME = "stream_cache"

    /**
     * The prefix to all cached file names.
     */
    private const val CACHED_FILE_PREFIX = "TMP"
}