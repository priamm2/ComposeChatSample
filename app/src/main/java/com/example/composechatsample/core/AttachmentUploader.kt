package com.example.composechatsample.core

import android.webkit.MimeTypeMap
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.log.taggedLogger
import java.io.File
import com.example.composechatsample.core.Result

public class AttachmentUploader(private val client: ChatClient = ChatClient.instance()) {

    private val logger by taggedLogger("Chat:Uploader")

    public suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        attachment: Attachment,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> {
        val file = checkNotNull(attachment.upload) { "An attachment needs to have a non null attachment.upload value" }

        val mimeType: String = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            ?: attachment.mimeType ?: ""
        val attachmentType = mimeType.toAttachmentType()

        return if (attachmentType == AttachmentType.IMAGE) {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as image" }
            uploadImage(
                channelType = channelType,
                channelId = channelId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
                mimeType = mimeType,
                attachmentType = attachmentType,
            )
        } else {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as file" }
            uploadFile(
                channelType = channelType,
                channelId = channelId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
                mimeType = mimeType,
                attachmentType = attachmentType,
            )
        }
    }

    @Suppress("LongParameterList")
    private suspend fun uploadImage(
        channelType: String,
        channelId: String,
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadImage] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                    "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.sendImage(channelType, channelId, file, progressCallback)
            .await()
        logger.v { "[uploadImage] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
                    mimeType = mimeType,
                    attachmentType = attachmentType,
                    uploadedFile = result.value,
                )

                onSuccessfulUpload(
                    augmentedAttachment = augmentedAttachment,
                    progressCallback = progressCallback,
                )
            }
            is Result.Failure -> {
                onFailedUpload(
                    attachment = attachment,
                    result = result,
                    progressCallback = progressCallback,
                )
            }
        }
    }

    @Suppress("LongParameterList")
    private suspend fun uploadFile(
        channelType: String,
        channelId: String,
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadFile] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                    "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.sendFile(channelType, channelId, file, progressCallback)
            .await()
        logger.v { "[uploadFile] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
                    mimeType = mimeType,
                    attachmentType = attachmentType,
                    uploadedFile = result.value,
                )

                onSuccessfulUpload(
                    augmentedAttachment = augmentedAttachment,
                    progressCallback = progressCallback,
                )
            }
            is Result.Failure -> {
                onFailedUpload(
                    attachment = attachment,
                    result = result,
                    progressCallback = progressCallback,
                )
            }
        }
    }

    private fun onSuccessfulUpload(
        augmentedAttachment: Attachment,
        progressCallback: ProgressCallback?,
    ): Result<Attachment> {
        logger.d { "[onSuccessfulUpload] #uploader; attachment ${augmentedAttachment.uploadId} uploaded successfully" }
        progressCallback?.onSuccess(augmentedAttachment.url)
        return Result.Success(augmentedAttachment.copy(uploadState = Attachment.UploadState.Success))
    }

    private fun onFailedUpload(
        attachment: Attachment,
        result: Result.Failure,
        progressCallback: ProgressCallback?,
    ): Result<Attachment> {
        logger.e { "[onFailedUpload] #uploader; attachment ${attachment.uploadId} upload failed: ${result.value}" }
        progressCallback?.onError(result.value)
        return Result.Failure(result.value)
    }

    private fun Attachment.augmentAttachmentOnSuccess(
        file: File,
        mimeType: String,
        attachmentType: AttachmentType,
        uploadedFile: UploadedFile,
    ): Attachment {
        return copy(
            name = file.name,
            fileSize = file.length().toInt(),
            mimeType = mimeType,
            url = uploadedFile.file,
            uploadState = Attachment.UploadState.Success,
            title = title.takeUnless { it.isNullOrBlank() } ?: file.name,
            thumbUrl = uploadedFile.thumbUrl,
            type = type ?: attachmentType.toString(),
            imageUrl = when (attachmentType) {
                AttachmentType.IMAGE -> uploadedFile.file
                AttachmentType.VIDEO -> uploadedFile.thumbUrl
                else -> imageUrl
            },
            assetUrl = when (attachmentType) {
                AttachmentType.IMAGE -> assetUrl
                else -> uploadedFile.file
            },
            extraData = extraData + uploadedFile.extraData,
        )
    }

    private fun String?.toAttachmentType(): AttachmentType {
        if (this == null) {
            return AttachmentType.FILE
        }
        return when {
            StreamCdnImageMimeTypes.isImageMimeTypeSupported(this) -> AttachmentType.IMAGE
            this.contains("video") -> AttachmentType.VIDEO
            else -> AttachmentType.FILE
        }
    }

    private enum class AttachmentType(private val value: String) {
        IMAGE("image"),
        VIDEO("video"),
        FILE("file"),
        ;

        override fun toString(): String {
            return value
        }
    }
}