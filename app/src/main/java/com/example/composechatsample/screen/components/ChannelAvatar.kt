package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.initials
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.PreviewChannelData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    groupAvatarTextStyle: TextStyle = ChatTheme.typography.captionBold,
    showOnlineIndicator: Boolean = true,
    onlineIndicatorAlignment: OnlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
    onlineIndicator: @Composable BoxScope.() -> Unit = {
        DefaultOnlineIndicator(onlineIndicatorAlignment)
    },
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val members = channel.members
    val memberCount = members.size

    when {
        channel.image.isNotEmpty() -> {
            Avatar(
                modifier = modifier,
                imageUrl = channel.image,
                initials = channel.initials,
                textStyle = textStyle,
                shape = shape,
                contentDescription = contentDescription,
                onClick = onClick,
            )
        }

        memberCount == 1 -> {
            val user = members.first().user

            UserAvatar(
                modifier = modifier,
                user = user,
                shape = shape,
                contentDescription = user.name,
                showOnlineIndicator = showOnlineIndicator,
                onlineIndicatorAlignment = onlineIndicatorAlignment,
                onlineIndicator = onlineIndicator,
                onClick = onClick,
            )
        }

        memberCount == 2 && members.any { it.user.id == currentUser?.id } -> {
            val user = members.first { it.user.id != currentUser?.id }.user

            UserAvatar(
                modifier = modifier,
                user = user,
                shape = shape,
                contentDescription = user.name,
                showOnlineIndicator = showOnlineIndicator,
                onlineIndicatorAlignment = onlineIndicatorAlignment,
                onlineIndicator = onlineIndicator,
                onClick = onClick,
            )
        }

        else -> {
            val users = members.filter { it.user.id != currentUser?.id }.map { it.user }

            GroupAvatar(
                users = users,
                modifier = modifier,
                shape = shape,
                textStyle = groupAvatarTextStyle,
                onClick = onClick,
            )
        }
    }
}

@Preview(showBackground = true, name = "ChannelAvatar Preview (With image)")
@Composable
private fun ChannelWithImageAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithImage)
}

@Preview(showBackground = true, name = "ChannelAvatar Preview (Online user)")
@Composable
private fun ChannelAvatarForDirectChannelWithOnlineUserPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithOnlineUser)
}

@Preview(showBackground = true, name = "ChannelAvatar Preview (Only one user)")
@Composable
private fun ChannelAvatarForDirectChannelWithOneUserPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithOneUser)
}

@Preview(showBackground = true, name = "ChannelAvatar Preview (Few members)")
@Composable
private fun ChannelAvatarForChannelWithFewMembersPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithFewMembers)
}

@Preview(showBackground = true, name = "ChannelAvatar Preview (Many members)")
@Composable
private fun ChannelAvatarForChannelWithManyMembersPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithManyMembers)
}

@Composable
private fun ChannelAvatarPreview(channel: Channel) {
    ChatTheme {
        ChannelAvatar(
            channel = channel,
            currentUser = PreviewUserData.user1,
            modifier = Modifier.size(36.dp),
        )
    }
}