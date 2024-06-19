package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import java.util.Date

internal object PreviewChannelData {

    val channelWithImage: Channel = Channel(
        type = "channelType",
        id = "channelId1",
        image = "https://picsum.photos/id/237/128/128",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        ),
    )

    val channelWithOneUser: Channel = Channel(
        type = "channelType",
        id = "channelId2",
        members = listOf(
            Member(user = PreviewUserData.user1),
        ),
    )

    val channelWithOnlineUser: Channel = Channel(
        type = "channelType",
        id = "channelId2",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2.copy(online = true)),
        ),
    )

    val channelWithFewMembers: Channel = Channel(
        type = "channelType",
        id = "channelId3",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
        ),
    )

    val channelWithManyMembers: Channel = Channel(
        type = "channelType",
        id = "channelId4",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
            Member(user = PreviewUserData.user4),
            Member(user = PreviewUserData.userWithoutImage),
        ),
    )

    val channelWithMessages: Channel = Channel(
        type = "channelType",
        id = "channelId5",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        ),
        messages = listOf(
            PreviewMessageData.message1,
            PreviewMessageData.message2,
        ),
        lastMessageAt = Date(),
    )
}