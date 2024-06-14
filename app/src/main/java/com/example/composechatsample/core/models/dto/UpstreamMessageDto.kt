package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val cid: String,
    val command: String?,
    val html: String,
    val id: String,
    val mentioned_users: List<String>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean,
    val pinned_at: Date?,
    val pinned_by: UpstreamUserDto?,
    val quoted_message_id: String?,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<UpstreamUserDto>,

    val extraData: Map<String, Any>,
) : ExtraDataDto