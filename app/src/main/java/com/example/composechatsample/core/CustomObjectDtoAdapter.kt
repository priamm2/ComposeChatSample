package com.example.composechatsample.core

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.reflect.KClass

internal open class CustomObjectDtoAdapter<Value : Any>(private val kClass: KClass<Value>) {

    private companion object {
        private const val EXTRA_DATA = "extraData"
    }

    private val memberNames: List<String> by lazy {
        kClass.members.map { member -> member.name }.minus(EXTRA_DATA)
    }

    protected fun parseWithExtraData(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<Value>,
    ): Value? {
        if (jsonReader.peek() == JsonReader.Token.NULL) {
            jsonReader.nextNull<Nothing?>()
            return null
        }

        val map = mapAdapter.fromJson(jsonReader)!!
        val extraData = mutableMapOf<String, Any>()
        map[EXTRA_DATA]?.let { explicitExtraData ->
            extraData[EXTRA_DATA] = explicitExtraData
        }

        map.forEach { entry ->
            if (entry.key !in memberNames) {
                extraData[entry.key] = entry.value
            }
        }
        map[EXTRA_DATA] = extraData
        return valueAdapter.fromJsonValue(map)!!
    }

    @Suppress("UNCHECKED_CAST")
    protected fun serializeWithExtraData(
        jsonWriter: JsonWriter,
        value: Value?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        valueAdapter: JsonAdapter<Value>,
    ) {
        if (value == null) {
            jsonWriter.nullValue()
            return
        }

        val map: MutableMap<String, Any?> = valueAdapter.toJsonValue(value) as MutableMap<String, Any?>
        val extraData = map[EXTRA_DATA] as Map<String, Any?>
        map.remove(EXTRA_DATA)
        map.putAll(extraData)
        mapAdapter.toJson(jsonWriter, map)
    }
}