package com.example.composechatsample.core.models.mapper

import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.dto.AttachmentDto
import com.example.composechatsample.core.models.dto.DeviceDto
import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.example.composechatsample.core.models.dto.UpstreamReactionDto
import com.example.composechatsample.core.models.dto.UpstreamUserDto

internal fun Message.toDto(): UpstreamMessageDto =
    UpstreamMessageDto(
        attachments = attachments.map(Attachment::toDto),
        cid = cid,
        command = command,
        html = html,
        id = id,
        mentioned_users = mentionedUsersIds,
        parent_id = parentId,
        pin_expires = pinExpires,
        pinned = pinned,
        pinned_at = pinnedAt,
        pinned_by = pinnedBy?.toDto(),
        quoted_message_id = replyMessageId,
        shadowed = shadowed,
        show_in_channel = showInChannel,
        silent = silent,
        text = text,
        thread_participants = threadParticipants.map(User::toDto),
        extraData = extraData,
    )

internal fun Attachment.toDto(): AttachmentDto =
    AttachmentDto(
        asset_url = assetUrl,
        author_name = authorName,
        fallback = fallback,
        file_size = fileSize,
        image = image,
        image_url = imageUrl,
        mime_type = mimeType,
        name = name,
        og_scrape_url = ogUrl,
        text = text,
        thumb_url = thumbUrl,
        title = title,
        title_link = titleLink,
        author_link = authorLink,
        type = type,
        url = url,
        original_height = originalHeight,
        original_width = originalWidth,
        extraData = extraData,
    )

internal fun User.toDto(): UpstreamUserDto =
    UpstreamUserDto(
        banned = isBanned,
        id = id,
        name = name,
        image = image,
        invisible = isInvisible,
        privacy_settings = privacySettings?.toDto(),
        language = language,
        role = role,
        devices = devices.map(Device::toDto),
        teams = teams,
        extraData = extraData,
    )

internal fun Device.toDto(): DeviceDto =
    DeviceDto(
        id = token,
        push_provider = pushProvider.key,
        provider_name = providerName,
    )

internal fun Reaction.toDto(): UpstreamReactionDto =
    UpstreamReactionDto(
        created_at = createdAt,
        message_id = messageId,
        score = score,
        type = type,
        updated_at = updatedAt,
        user = user?.toDto(),
        user_id = userId,
        extraData = extraData,
    )