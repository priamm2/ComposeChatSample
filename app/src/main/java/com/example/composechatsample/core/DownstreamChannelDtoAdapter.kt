package com.example.composechatsample.core

import com.example.composechatsample.core.models.dto.DownstreamChannelDto
import com.example.composechatsample.core.models.dto.UpstreamChannelDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal object DownstreamChannelDtoAdapter :
    CustomObjectDtoAdapter<DownstreamChannelDto>(DownstreamChannelDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<DownstreamChannelDto>,
    ): DownstreamChannelDto? = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: DownstreamChannelDto): Unit = error("Can't convert this to Json")
}

internal object UpstreamChannelDtoAdapter :
    CustomObjectDtoAdapter<UpstreamChannelDto>(UpstreamChannelDto::class) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): UpstreamChannelDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UpstreamChannelDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UpstreamChannelDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}