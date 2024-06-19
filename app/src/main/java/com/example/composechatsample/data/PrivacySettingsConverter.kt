package com.example.composechatsample.data

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class PrivacySettingsConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val entityAdapter = moshi.adapter<PrivacySettingsEntity>()

    @TypeConverter
    fun stringToPrivacySettings(data: String?): PrivacySettingsEntity? {
        return data?.let {
            entityAdapter.fromJson(it)
        }
    }

    @TypeConverter
    fun privacySettingsToString(entity: PrivacySettingsEntity?): String? {
        return entity?.let {
            entityAdapter.toJson(it)
        }
    }
}