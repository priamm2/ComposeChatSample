package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val name: String?,
    val image: String?,
    val watcher_count: Int = 0,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int = 0,
    val messages: List<DownstreamMessageDto> = emptyList(),
    val members: List<DownstreamMemberDto> = emptyList(),
    val watchers: List<DownstreamUserDto> = emptyList(),
    val read: List<DownstreamChannelUserRead> = emptyList(),
    val config: ConfigDto,
    val created_by: DownstreamUserDto?,
    val team: String = "",
    val cooldown: Int = 0,
    val pinned_messages: List<DownstreamMessageDto> = emptyList(),
    val own_capabilities: List<String> = emptyList(),
    val membership: DownstreamMemberDto?,

    val extraData: Map<String, Any>,
) : ExtraDataDto