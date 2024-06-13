package com.example.composechatsample.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = USER_ENTITY_TABLE_NAME)
internal data class UserEntity(
    @PrimaryKey val id: String,
    val originalId: String = "",
    @ColumnInfo(index = true)
    val name: String,
    val image: String,
    val role: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val lastActive: Date? = null,
    val invisible: Boolean = false,
    val privacySettings: PrivacySettingsEntity?,
    val banned: Boolean = false,
    val mutes: List<String> = emptyList(),
    val extraData: Map<String, Any> = emptyMap(),
)

internal const val USER_ENTITY_TABLE_NAME = "stream_chat_user"