package com.example.composechatsample.core

import com.example.composechatsample.core.models.requests.FlagMessageRequest
import com.example.composechatsample.core.models.requests.FlagRequest
import com.example.composechatsample.core.models.requests.FlagUserRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal object FlagRequestAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when (type) {
            FlagRequest::class.java -> FlagRequestAdapter(moshi)
            else -> null
        }

    private class FlagRequestAdapter(private val moshi: Moshi) : JsonAdapter<FlagRequest>() {
        override fun fromJson(reader: JsonReader): FlagRequest? {
            reader.readJsonValue()
            return null
        }

        override fun toJson(writer: JsonWriter, value: FlagRequest?) {
            when (value) {
                is FlagMessageRequest -> moshi.adapter(FlagMessageRequest::class.java).toJson(writer, value)
                is FlagUserRequest -> moshi.adapter(FlagUserRequest::class.java).toJson(writer, value)
                else -> {}
            }
        }
    }
}