package com.example.composechatsample.core

import com.example.composechatsample.core.models.UploadedFile
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal class StreamFileUploader(
    private val retrofitCdnApi: RetrofitCdnApi,
) : FileUploader {

    private val filenameSanitizer = FilenameSanitizer()

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).execute().map {
            it.toUploadedFile()
        }
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendFile(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).execute().map {
            it.toUploadedFile()
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = callback,
        ).execute().map {
            UploadedFile(file = it.file)
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile> {
        val body = file.asRequestBody(file.getMediaType())
        val filename = filenameSanitizer.sanitize(file.name)
        val part = MultipartBody.Part.createFormData("file", filename, body)

        return retrofitCdnApi.sendImage(
            channelType = channelType,
            channelId = channelId,
            file = part,
            progressCallback = null,
        ).execute().map {
            UploadedFile(file = it.file)
        }
    }

    override fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteFile(
            channelType = channelType,
            channelId = channelId,
            url = url,
        ).execute().toUnitResult()
    }

    override fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit> {
        return retrofitCdnApi.deleteImage(
            channelType = channelType,
            channelId = channelId,
            url = url,
        ).execute().toUnitResult()
    }
}

private class FilenameSanitizer {
    companion object {
        private const val MAX_NAME_LEN = 255
        private const val EMPTY = ""
    }

    private val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '-' + '_'

    fun sanitize(filename: String): String = try {
        sanitizeInternal(filename)
    } catch (_: Throwable) {
        filename
    }

    private fun sanitizeInternal(filename: String): String {
        val extension = filename.substringAfterLast(delimiter = '.', missingDelimiterValue = EMPTY)
        val baseName = if (extension.isNotEmpty()) filename.removeSuffix(suffix = ".$extension") else filename
        var sanitizedBaseName = baseName.map { if (it in allowedChars) it else '_' }.joinToString(EMPTY)

        val maxBaseNameLength = MAX_NAME_LEN - extension.length - 1
        if (sanitizedBaseName.length > maxBaseNameLength) {
            sanitizedBaseName = sanitizedBaseName.substring(0, maxBaseNameLength)
        }
        return if (extension.isNotEmpty()) "$sanitizedBaseName.$extension" else sanitizedBaseName
    }
}