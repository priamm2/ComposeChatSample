package com.example.composechatsample.core

import com.example.composechatsample.core.models.UploadedFile
import java.io.File

public interface FileUploader {

    @Suppress("LongParameterList")
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile>

    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile>

    @Suppress("LongParameterList")
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile>


    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile>

    public fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit>


    public fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit>
}