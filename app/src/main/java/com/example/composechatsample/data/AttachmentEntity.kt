package com.example.composechatsample.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.composechatsample.data.AttachmentEntity.Companion.ATTACHMENT_ENTITY_TABLE_NAME

@Entity(
    tableName = ATTACHMENT_ENTITY_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
    indices = [Index("messageId")],
)
data class AttachmentEntity(
    @ColumnInfo(index = true)
    @PrimaryKey
    val id: String,
    val messageId: String,
    val authorName: String?,
    val titleLink: String?,
    val authorLink: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val assetUrl: String?,
    val ogUrl: String?,
    val mimeType: String?,
    val fileSize: Int,
    val title: String?,
    val text: String?,
    val type: String?,
    val image: String?,
    val url: String?,
    val name: String?,
    val fallback: String?,
    val uploadFilePath: String?,
    var originalHeight: Int?,
    var originalWidth: Int?,
    @Embedded
    var uploadState: UploadStateEntity? = null,
    val extraData: Map<String, Any>,
) {
    companion object {
        internal const val EXTRA_DATA_ID_KEY = "extra_data_id_key"
        internal const val ATTACHMENT_ENTITY_TABLE_NAME = "attachment_inner_entity"
        internal fun generateId(messageId: String, index: Int): String {
            return messageId + "_$index"
        }
    }
}

data class UploadStateEntity(val statusCode: Int, val errorMessage: String?) {
    internal companion object {
        internal const val UPLOAD_STATE_SUCCESS = 1
        internal const val UPLOAD_STATE_IN_PROGRESS = 2
        internal const val UPLOAD_STATE_FAILED = 3
    }
}