package com.example.composechatsample.core

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.querysort.QuerySorter

internal fun QueryChannelIdentifier(
    channelType: String,
    channelId: String,
    request: QueryChannelRequest,
): Int {
    var result = "QueryChannel".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + request.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun QueryChannelsIdentifier(
    request: QueryChannelsRequest,
): Int {
    var result = "QueryChannels".hashCode()
    result = 31 * result + request.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber", "LongParameterList")
internal fun QueryMembersIdentifier(
    channelType: String,
    channelId: String,
    offset: Int,
    limit: Int,
    filter: FilterObject,
    sort: QuerySorter<Member>,
    members: List<Member> = emptyList(),
): Int {
    var result = "QueryMembers".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + offset.hashCode()
    result = 31 * result + limit.hashCode()
    result = 31 * result + filter.hashCode()
    result = 31 * result + sort.hashCode()
    result = 31 * result + members.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun DeleteReactionIdentifier(
    messageId: String,
    reactionType: String,
    cid: String?,
): Int {
    var result = "DeleteReaction".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + reactionType.hashCode()
    result = 31 * result + (cid?.hashCode() ?: 0)
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun SendReactionIdentifier(
    reaction: Reaction,
    enforceUnique: Boolean,
    cid: String?,
): Int {
    var result = "SendReaction".hashCode()
    result = 31 * result + reaction.hashCode()
    result = 31 * result + enforceUnique.hashCode()
    result = 31 * result + cid.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesIdentifier(
    messageId: String,
    limit: Int,
): Int {
    var result = "GetReplies".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + limit.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun getNewerRepliesIdentifier(
    parentId: String,
    limit: Int,
    lastId: String? = null,
): Int {
    var result = "GetOlderReplies".hashCode()
    result = 31 * result + parentId.hashCode()
    result = 31 * result + limit.hashCode()
    result = 31 * result + (lastId?.hashCode() ?: 0)
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesMoreIdentifier(
    messageId: String,
    firstId: String,
    limit: Int,
): Int {
    var result = "GetRepliesMore".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + firstId.hashCode()
    result = 31 * result + limit.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun SendGiphyIdentifier(
    request: SendActionRequest,
): Int {
    var result = "SendGiphy".hashCode()
    result = 31 * result + request.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun ShuffleGiphyIdentifier(
    request: SendActionRequest,
): Int {
    var result = "ShuffleGiphy".hashCode()
    result = 31 * result + request.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun DeleteMessageIdentifier(
    messageId: String,
    hard: Boolean,
): Int {
    var result = "DeleteMessage".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + hard.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun SendEventIdentifier(
    eventType: String,
    channelType: String,
    channelId: String,
    parentId: String?,
): Int {
    var result = "SendEvent".hashCode()
    result = 31 * result + eventType.hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + parentId.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun UpdateMessageIdentifier(
    message: Message,
): Int {
    var result = "UpdateMessage".hashCode()
    result = 31 * result + message.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun HideChannelIdentifier(
    channelType: String,
    channelId: String,
    clearHistory: Boolean,
): Int {
    var result = "HideChannel".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + clearHistory.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun GetMessageIdentifier(
    messageId: String,
): Int {
    var result = "GetMessage".hashCode()
    result = 31 * result + messageId.hashCode()

    return result
}

@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun MarkAllReadIdentifier(): Int {
    return "MarkAllRead".hashCode()
}

@Suppress("FunctionName", "MagicNumber")
internal fun MarkReadIdentifier(
    channelType: String,
    channelId: String,
): Int {
    var result = "MarkRead".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    return result
}

@Suppress("FunctionName", "MagicNumber")
internal fun SendMessageIdentifier(
    channelType: String,
    channelId: String,
    messageId: String,
): Int {
    var result = "SendMessage".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + messageId.hashCode()
    return result
}

@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun GetDevicesIdentifier(): Int {
    return "GetDevices".hashCode()
}

@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun AddDeviceIdentifier(
    device: Device,
): Int {
    var result = "AddDevice".hashCode()
    result = 31 * result + device.hashCode()
    return result
}

@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun DeleteDeviceIdentifier(
    device: Device,
): Int {
    var result = "DeleteDevice".hashCode()
    result = 31 * result + device.hashCode()
    return result
}