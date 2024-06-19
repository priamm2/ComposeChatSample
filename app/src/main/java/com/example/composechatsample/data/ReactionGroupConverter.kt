package com.example.composechatsample.data

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class ReactionGroupConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val entityAdapter = moshi.adapter<ReactionGroupEntity>()

    @OptIn(ExperimentalStdlibApi::class)
    private val entityMapAdapter = moshi.adapter<Map<String, ReactionGroupEntity>>()

    @TypeConverter
    fun stringToReactionGroupEntity(data: String?): ReactionGroupEntity? {
        return data?.let {
            entityAdapter.fromJson(it)
        }
    }

    @TypeConverter
    fun reactionGroupEntityToString(entity: ReactionGroupEntity?): String? {
        return entity?.let {
            entityAdapter.toJson(it)
        }
    }

    @TypeConverter
    fun reactionGroupEntityMapToString(entities: Map<String, ReactionGroupEntity>?): String? {
        return entities?.let {
            entityMapAdapter.toJson(it)
        }
    }

    @TypeConverter
    fun stringToReactionGroupEntityMap(data: String?): Map<String, ReactionGroupEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return entityMapAdapter.fromJson(data)
    }
}