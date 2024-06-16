package com.example.composechatsample.core

import com.example.composechatsample.core.models.dto.DownstreamMessageDto
import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal object DownstreamMessageDtoAdapter :
    CustomObjectDtoAdapter<DownstreamMessageDto>(DownstreamMessageDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<DownstreamMessageDto>,
    ): DownstreamMessageDto? = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: DownstreamMessageDto): Unit = error("Can't convert this to Json")
}

internal object UpstreamMessageDtoAdapter :
    CustomObjectDtoAdapter<UpstreamMessageDto>(UpstreamMessageDto::class) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): UpstreamMessageDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UpstreamMessageDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UpstreamMessageDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}