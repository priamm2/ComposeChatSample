package com.example.composechatsample.core

import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.data.DateAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit

internal class MoshiChatParser : ChatParser {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addAdapter(DateAdapter())
            .addAdapter(ExactDateAdapter())
            .add(EventAdapterFactory())
            .add(DownstreamMessageDtoAdapter)
            .add(DownstreamModerationDetailsDtoAdapter)
            .add(UpstreamMessageDtoAdapter)
            .add(DownstreamChannelDtoAdapter)
            .add(UpstreamChannelDtoAdapter)
            .add(AttachmentDtoAdapter)
            .add(DownstreamReactionDtoAdapter)
            .add(UpstreamReactionDtoAdapter)
            .add(DownstreamUserDtoAdapter)
            .add(UpstreamUserDtoAdapter)
            .add(FlagRequestAdapterFactory)
            .build()
    }

    private inline fun <reified T> Moshi.Builder.addAdapter(adapter: JsonAdapter<T>) = apply {
        this.add(T::class.java, adapter)
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder
            .addConverterFactory(MoshiUrlQueryPayloadFactory(moshi))
            .addConverterFactory(MoshiConverterFactory.create(moshi).withErrorLogging())
    }

    override fun toJson(any: Any): String = when {
        Map::class.java.isAssignableFrom(any.javaClass) -> serializeMap(any)
        any is ConnectedEvent -> serializeConnectedEvent(any)
        else -> moshi.adapter(any.javaClass).toJson(any)
    }

    private val mapAdapter = moshi.adapter(Map::class.java)

    private fun serializeMap(any: Any): String {
        return mapAdapter.toJson(any as Map<*, *>)
    }

    private val upstreamConnectedEventAdapter = moshi.adapter(UpstreamConnectedEventDto::class.java)

    private fun serializeConnectedEvent(connectedEvent: ConnectedEvent): String {
        val eventDto = connectedEvent.toDto()
        return upstreamConnectedEventAdapter.toJson(eventDto)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        return when (clazz) {
            ChatEvent::class.java -> parseAndProcessEvent(raw) as T
            SocketErrorMessage::class.java -> parseSocketError(raw) as T
            ErrorResponse::class.java -> parseErrorResponse(raw) as T
            else -> return moshi.adapter(clazz).fromJson(raw)!!
        }
    }

    private val socketErrorResponseAdapter = moshi.adapter(SocketErrorResponse::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseSocketError(raw: String): SocketErrorMessage {
        return socketErrorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val errorResponseAdapter = moshi.adapter(SocketErrorResponse.ErrorResponse::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseErrorResponse(raw: String): ErrorResponse {
        return errorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val chatEventDtoAdapter = moshi.adapter(ChatEventDto::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseAndProcessEvent(raw: String): ChatEvent {
        val event = chatEventDtoAdapter.fromJson(raw)!!.toDomain()
        return event.enrichIfNeeded()
    }
}