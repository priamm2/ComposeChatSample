package com.example.composechatsample.screen.channels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composechatsample.R
import com.example.composechatsample.core.getLastMessage
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.mapper.currentUserUnreadCount
import com.example.composechatsample.screen.PreviewChannelData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.components.ChannelAvatar
import com.example.composechatsample.screen.components.MessageReadStatusIcon
import com.example.composechatsample.screen.components.Timestamp
import com.example.composechatsample.screen.components.UnreadCountIndicator
import com.example.composechatsample.ui.theme.ChatTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ChannelItem(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        DefaultChannelItemLeadingContent(
            channelItem = it,
            currentUser = currentUser,
        )
    },
    centerContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        DefaultChannelItemCenterContent(
            channel = it.channel,
            isMuted = it.isMuted,
            currentUser = currentUser,
        )
    },
    trailingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        DefaultChannelItemTrailingContent(
            channel = it.channel,
            currentUser = currentUser,
        )
    },
) {
    val channel = channelItem.channel
    val description = stringResource(id = R.string.stream_compose_cd_channel_item)

    Column(
        modifier = modifier
            .testTag("Stream_ChannelItem")
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { contentDescription = description }
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(channelItem)

            centerContent(channelItem)

            trailingContent(channelItem)
        }
    }
}

@Composable
internal fun DefaultChannelItemLeadingContent(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
) {
    ChannelAvatar(
        modifier = Modifier
            .padding(
                start = ChatTheme.dimens.channelItemHorizontalPadding,
                end = 4.dp,
                top = ChatTheme.dimens.channelItemVerticalPadding,
                bottom = ChatTheme.dimens.channelItemVerticalPadding,
            )
            .size(ChatTheme.dimens.channelAvatarSize),
        channel = channelItem.channel,
        currentUser = currentUser,
    )
}

@Composable
internal fun RowScope.DefaultChannelItemCenterContent(
    channel: Channel,
    isMuted: Boolean,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        val channelName: (@Composable (modifier: Modifier) -> Unit) = @Composable {
            Text(
                modifier = it,
                text = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
                style = ChatTheme.typography.bodyBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        if (isMuted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                channelName(Modifier.weight(weight = 1f, fill = false))

                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            }
        } else {
            channelName(Modifier)
        }

        val lastMessageText = channel.getLastMessage(currentUser)?.let { lastMessage ->
            ChatTheme.messagePreviewFormatter.formatMessagePreview(lastMessage, currentUser)
        } ?: AnnotatedString("")

        if (lastMessageText.isNotEmpty()) {
            Text(
                text = lastMessageText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

@Composable
internal fun RowScope.DefaultChannelItemTrailingContent(
    channel: Channel,
    currentUser: User?,
) {
    val lastMessage = channel.getLastMessage(currentUser)

    if (lastMessage != null) {
        Column(
            modifier = Modifier
                .padding(
                    start = 4.dp,
                    end = ChatTheme.dimens.channelItemHorizontalPadding,
                    top = ChatTheme.dimens.channelItemVerticalPadding,
                    bottom = ChatTheme.dimens.channelItemVerticalPadding,
                )
                .wrapContentHeight()
                .align(Alignment.Bottom),
            horizontalAlignment = Alignment.End,
        ) {
            val unreadCount = channel.currentUserUnreadCount

            if (unreadCount > 0) {
                UnreadCountIndicator(
                    modifier = Modifier.padding(bottom = 4.dp),
                    unreadCount = unreadCount,
                )
            }

            val isLastMessageFromCurrentUser = lastMessage.user.id == currentUser?.id

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLastMessageFromCurrentUser) {
                    MessageReadStatusIcon(
                        channel = channel,
                        message = lastMessage,
                        currentUser = currentUser,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .heightIn(16.dp),
                    )
                }

                Timestamp(date = channel.lastUpdated)
            }
        }
    }
}

@Preview(showBackground = true, name = "ChannelItem Preview (Channel with unread)")
@Composable
private fun ChannelItemForChannelWithUnreadMessagesPreview() {
    ChannelItemPreview(
        channel = PreviewChannelData.channelWithMessages,
        currentUser = PreviewUserData.user1,
    )
}

@Preview(showBackground = true, name = "ChannelItem Preview (Muted channel)")
@Composable
private fun ChannelItemForMutedChannelPreview() {
    ChannelItemPreview(
        channel = PreviewChannelData.channelWithMessages,
        currentUser = PreviewUserData.user1,
        isMuted = true,
    )
}

@Preview(showBackground = true, name = "ChannelItem Preview (Without messages)")
@Composable
private fun ChannelItemForChannelWithoutMessagesPreview() {
    ChannelItemPreview(
        channel = PreviewChannelData.channelWithImage,
        isMuted = false,
        currentUser = PreviewUserData.user1,
    )
}

@Composable
private fun ChannelItemPreview(
    channel: Channel,
    isMuted: Boolean = false,
    currentUser: User? = null,
) {
    ChatTheme {
        ChannelItem(
            channelItem = ItemState.ChannelItemState(
                channel = channel,
                isMuted = isMuted,
            ),
            currentUser = currentUser,
            onChannelClick = {},
            onChannelLongClick = {},
        )
    }
}