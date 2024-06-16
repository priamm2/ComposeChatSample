package com.example.composechatsample.core

import com.example.composechatsample.core.errors.ErrorDetail
import com.example.composechatsample.core.errors.ErrorResponse
import com.example.composechatsample.core.errors.SocketErrorMessage
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.models.dto.ChatEventDto
import com.example.composechatsample.core.models.dto.SocketErrorResponse
import com.example.composechatsample.core.models.dto.UpstreamConnectedEventDto
import com.example.composechatsample.core.models.mapper.toDomain
import com.example.composechatsample.core.models.mapper.toDto
import com.example.composechatsample.data.DateAdapter
import com.example.composechatsample.data.ExactDateAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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

    private fun parseSocketError(raw: String): SocketErrorMessage {
        return socketErrorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val errorResponseAdapter = moshi.adapter(ErrorResponse::class.java)

    private fun parseErrorResponse(raw: String): ErrorResponse {
        return errorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    fun ErrorResponse.toDomain(): ErrorResponse {
        val dto = this
        return ErrorResponse(
            code = dto.code,
            message = dto.message,
            statusCode = dto.statusCode,
            exceptionFields = dto.exceptionFields,
            moreInfo = dto.moreInfo,
            details = dto.details.map { it.toDomain() },
        ).apply {
            duration = dto.duration
        }
    }

    fun ErrorDetail.toDomain(): ErrorDetail {
        val dto = this
        return ErrorDetail(
            code = dto.code,
            messages = dto.messages,
        )
    }

    private val chatEventDtoAdapter = moshi.adapter(ChatEventDto::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseAndProcessEvent(raw: String): ChatEvent {
        val event = chatEventDtoAdapter.fromJson(raw)!!.toDomain()
        return event.enrichIfNeeded()
    }
}