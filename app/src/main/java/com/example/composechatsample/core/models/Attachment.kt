package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.CustomObject
import java.io.File

@Immutable
public data class Attachment(
    val authorName: String? = null,
    val authorLink: String? = null,
    val titleLink: String? = null,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val assetUrl: String? = null,
    val ogUrl: String? = null,
    val mimeType: String? = null,
    val fileSize: Int = 0,
    val title: String? = null,
    val text: String? = null,
    val type: String? = null,
    val image: String? = null,
    val url: String? = null,
    val name: String? = null,
    val fallback: String? = null,
    val originalHeight: Int? = null,
    val originalWidth: Int? = null,

    val upload: File? = null,

    val uploadState: UploadState? = null,

    override val extraData: Map<String, Any> = mapOf(),

) : CustomObject {

    public sealed class UploadState {
        public object Idle : UploadState() { override fun toString(): String = "Idle" }
        public data class InProgress(val bytesUploaded: Long, val totalBytes: Long) : UploadState()
        public object Success : UploadState() { override fun toString(): String = "Success" }
        public data class Failed(val error: Error) : UploadState()
    }

    override fun toString(): String = StringBuilder().apply {
        append("Attachment(")
        append("mimeType=\"").append(mimeType).append("\"")
        if (authorName != null) append(", authorName=").append(authorName)
        if (authorLink != null) append(", authorLink=").append(authorLink)
        if (titleLink != null) append(", titleLink=").append(titleLink)
        if (thumbUrl != null) append(", thumbUrl=").append(thumbUrl.shorten())
        if (imageUrl != null) append(", imageUrl=").append(imageUrl.shorten())
        if (assetUrl != null) append(", assetUrl=").append(assetUrl.shorten())
        if (ogUrl != null) append(", ogUrl=").append(ogUrl.hashCode())
        if (fileSize > 0) append(", fileSize=").append(fileSize)
        if (title != null) append(", title=\"").append(title).append("\"")
        if (text != null) append(", text=\"").append(text).append("\"")
        if (type != null) append(", type=\"").append(type).append("\"")
        if (image != null) append(", image=").append(image)
        if (url != null) append(", url=").append(url.shorten())
        if (name != null) append(", name=").append(name)
        if (fallback != null) append(", fallback=").append(fallback)
        if (originalHeight != null) append(", origH=").append(originalHeight)
        if (originalWidth != null) append(", origW=").append(originalWidth)
        if (upload != null) append(", upload=\"").append(upload).append("\"")
        if (uploadState != null) append(", uploadState=").append(uploadState)
        if (extraData.isNotEmpty()) append(", extraData=").append(extraData)
        append(")")
    }.toString()

    private fun String.shorten(): String {
        val min = 0
        val max = 9
        if (length <= max) return this
        return substring(min..max).let { "$it..." }
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var authorName: String? = null
        private var authorLink: String? = null
        private var titleLink: String? = null
        private var thumbUrl: String? = null
        private var imageUrl: String? = null
        private var assetUrl: String? = null
        private var ogUrl: String? = null
        private var mimeType: String? = null
        private var fileSize: Int = 0
        private var title: String? = null
        private var text: String? = null
        private var type: String? = null
        private var image: String? = null
        private var url: String? = null
        private var name: String? = null
        private var fallback: String? = null
        private var originalHeight: Int? = null
        private var originalWidth: Int? = null
        private var upload: File? = null
        private var uploadState: UploadState? = null
        private var extraData: Map<String, Any> = mapOf()

        public constructor(attachment: Attachment) : this() {
            authorName = attachment.authorName
            authorLink = attachment.authorLink
            titleLink = attachment.titleLink
            thumbUrl = attachment.thumbUrl
            imageUrl = attachment.imageUrl
            assetUrl = attachment.assetUrl
            ogUrl = attachment.ogUrl
            mimeType = attachment.mimeType
            fileSize = attachment.fileSize
            title = attachment.title
            text = attachment.text
            type = attachment.type
            image = attachment.image
            url = attachment.url
            name = attachment.name
            fallback = attachment.fallback
            originalHeight = attachment.originalHeight
            originalWidth = attachment.originalWidth
            upload = attachment.upload
            uploadState = attachment.uploadState
            extraData = attachment.extraData
        }
        public fun withAuthorName(authorName: String?): Builder = apply { this.authorName = authorName }
        public fun withAuthorLink(authorLink: String?): Builder = apply { this.authorLink = authorLink }
        public fun withTitleLink(titleLink: String?): Builder = apply { this.titleLink = titleLink }
        public fun withThumbUrl(thumbUrl: String?): Builder = apply { this.thumbUrl = thumbUrl }
        public fun withImageUrl(imageUrl: String?): Builder = apply { this.imageUrl = imageUrl }
        public fun withAssetUrl(assetUrl: String?): Builder = apply { this.assetUrl = assetUrl }
        public fun withOgUrl(ogUrl: String?): Builder = apply { this.ogUrl = ogUrl }
        public fun withMimeType(mimeType: String?): Builder = apply { this.mimeType = mimeType }
        public fun withFileSize(fileSize: Int): Builder = apply { this.fileSize = fileSize }
        public fun withTitle(title: String?): Builder = apply { this.title = title }
        public fun withText(text: String?): Builder = apply { this.text = text }
        public fun withType(type: String?): Builder = apply { this.type = type }
        public fun withImage(image: String?): Builder = apply { this.image = image }
        public fun withUrl(url: String?): Builder = apply { this.url = url }
        public fun withName(name: String?): Builder = apply { this.name = name }
        public fun withFallback(fallback: String?): Builder = apply { this.fallback = fallback }
        public fun withOriginalHeight(originalHeight: Int?): Builder = apply { this.originalHeight = originalHeight }
        public fun withOriginalWidth(originalWidth: Int?): Builder = apply { this.originalWidth = originalWidth }
        public fun withUpload(upload: File?): Builder = apply { this.upload = upload }
        public fun withUploadState(uploadState: UploadState?): Builder = apply { this.uploadState = uploadState }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }

        public fun build(): Attachment = Attachment(
            authorName = authorName,
            authorLink = authorLink,
            titleLink = titleLink,
            thumbUrl = thumbUrl,
            imageUrl = imageUrl,
            assetUrl = assetUrl,
            ogUrl = ogUrl,
            mimeType = mimeType,
            fileSize = fileSize,
            title = title,
            text = text,
            type = type,
            image = image,
            url = url,
            name = name,
            fallback = fallback,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            upload = upload,
            uploadState = uploadState,
            extraData = extraData,
        )
    }
}
