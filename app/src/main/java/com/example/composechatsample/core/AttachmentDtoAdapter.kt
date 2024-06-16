package com.example.composechatsample.core

import com.example.composechatsample.core.models.dto.AttachmentDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal object AttachmentDtoAdapter : CustomObjectDtoAdapter<AttachmentDto>(AttachmentDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<AttachmentDto>,
    ): AttachmentDto? = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: AttachmentDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<AttachmentDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}