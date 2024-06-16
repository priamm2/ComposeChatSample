package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val name: String,
    val image: String,
    val watcher_count: Int,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int,
    val messages: List<UpstreamMessageDto>,
    val members: List<UpstreamMemberDto>,
    val watchers: List<UpstreamUserDto>,
    val read: List<UpstreamChannelUserRead>,
    val config: ConfigDto,
    val created_by: UpstreamUserDto,
    val team: String,
    val cooldown: Int,
    val pinned_messages: List<UpstreamMessageDto>,

    val extraData: Map<String, Any>,
) : ExtraDataDto
