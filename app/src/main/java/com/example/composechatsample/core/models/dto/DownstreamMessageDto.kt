package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class DownstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val channel: ChannelInfoDto?,
    val cid: String,
    val command: String?,
    val created_at: Date,
    val deleted_at: Date?,
    val html: String,
    val i18n: Map<String, String> = emptyMap(),
    val id: String,
    val latest_reactions: List<DownstreamReactionDto>,
    val mentioned_users: List<DownstreamUserDto>,
    val own_reactions: List<DownstreamReactionDto>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean = false,
    val pinned_at: Date?,
    val message_text_updated_at: Date?,
    val pinned_by: DownstreamUserDto?,
    val quoted_message: DownstreamMessageDto?,
    val quoted_message_id: String?,
    val reaction_counts: Map<String, Int>?,
    val reaction_scores: Map<String, Int>?,
    val reaction_groups: Map<String, DownstreamReactionGroupDto>?,
    val reply_count: Int,
    val deleted_reply_count: Int,
    val shadowed: Boolean = false,
    val show_in_channel: Boolean = false,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<DownstreamUserDto> = emptyList(),
    val type: String,
    val updated_at: Date,
    val user: DownstreamUserDto,
    val moderation_details: DownstreamModerationDetailsDto? = null,

    val extraData: Map<String, Any>,
) : ExtraDataDto