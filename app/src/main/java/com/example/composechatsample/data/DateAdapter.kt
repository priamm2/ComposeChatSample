package com.example.composechatsample.data

import com.example.composechatsample.core.StreamDateFormatter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.util.Date

class DateAdapter : JsonAdapter<Date>() {

    private val streamDateFormatter = StreamDateFormatter("DateAdapter", cacheEnabled = true)

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val rawValue = streamDateFormatter.format(value)
            writer.value(rawValue)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @FromJson
    override fun fromJson(reader: JsonReader): Date? {
        val nextValue = reader.peek()
        if (nextValue == JsonReader.Token.NULL) {
            reader.skipValue()
            return null
        }

        val rawValue = reader.nextString()
        return streamDateFormatter.parse(rawValue)
    }
}