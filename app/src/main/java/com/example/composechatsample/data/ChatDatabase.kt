package com.example.composechatsample.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        QueryChannelsEntity::class,
        MessageInnerEntity::class,
        ReplyMessageInnerEntity::class,
        AttachmentEntity::class,
        ReplyAttachmentEntity::class,
        UserEntity::class,
        ReactionEntity::class,
        ChannelEntity::class,
        ChannelConfigInnerEntity::class,
        CommandInnerEntity::class,
        SyncStateEntity::class,
    ],
    version = 75,
    exportSchema = false,
)
@TypeConverters(
    FilterObjectConverter::class,
    ListConverter::class,
    MapConverter::class,
    QuerySortConverter::class,
    ExtraDataConverter::class,
    SetConverter::class,
    SyncStatusConverter::class,
    DateConverter::class,
    MemberConverter::class,
    ModerationDetailsConverter::class,
    ReactionGroupConverter::class,
    PrivacySettingsConverter::class,
)
internal abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsDao(): QueryChannelsDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao
    abstract fun replyMessageDao(): ReplyMessageDao
    abstract fun channelStateDao(): ChannelDao
    abstract fun channelConfigDao(): ChannelConfigDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCES: MutableMap<String, ChatDatabase?> = mutableMapOf()

        fun getDatabase(context: Context, userId: String): ChatDatabase {
            if (!INSTANCES.containsKey(userId)) {
                synchronized(this) {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        ChatDatabase::class.java,
                        "stream_chat_database_$userId",
                    ).fallbackToDestructiveMigration()
                        .addCallback(
                            object : Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    db.execSQL("PRAGMA synchronous = 1")
                                }
                            },
                        )
                        .build()
                    INSTANCES[userId] = db
                }
            }
            return INSTANCES[userId] ?: error("DB not created")
        }
    }
}