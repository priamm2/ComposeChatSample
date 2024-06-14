package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamChannelDto
import com.example.composechatsample.core.models.dto.DownstreamChannelUserRead
import com.example.composechatsample.core.models.dto.DownstreamMemberDto
import com.example.composechatsample.core.models.dto.DownstreamMessageDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class ChannelResponse(
    val channel: DownstreamChannelDto,
    val messages: List<DownstreamMessageDto> = emptyList(),
    val members: List<DownstreamMemberDto> = emptyList(),
    val membership: DownstreamMemberDto?,
    val watchers: List<DownstreamUserDto> = emptyList(),
    val read: List<DownstreamChannelUserRead> = emptyList(),
    val watcher_count: Int = 0,
    val hidden: Boolean?,
    val hide_messages_before: Date?,
)