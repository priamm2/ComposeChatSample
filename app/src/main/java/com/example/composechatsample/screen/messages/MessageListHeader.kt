package com.example.composechatsample.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.common.mirrorRtl
import com.example.composechatsample.core.getMembersStatusText
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.PreviewChannelData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.components.BackButton
import com.example.composechatsample.screen.components.ChannelAvatar
import com.example.composechatsample.screen.components.NetworkLoadingIndicator
import com.example.composechatsample.screen.components.TypingIndicator
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageListHeader(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    color: Color = ChatTheme.colors.barsBackground,
    shape: Shape = ChatTheme.shapes.header,
    elevation: Dp = ChatTheme.dimens.headerElevation,
    onBackPressed: () -> Unit = {},
    onHeaderTitleClick: (Channel) -> Unit = {},
    onChannelAvatarClick: () -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        DefaultMessageListHeaderLeadingContent(onBackPressed = onBackPressed)
    },
    centerContent: @Composable RowScope.() -> Unit = {
        DefaultMessageListHeaderCenterContent(
            modifier = Modifier.weight(1f),
            channel = channel,
            currentUser = currentUser,
            typingUsers = typingUsers,
            messageMode = messageMode,
            onHeaderTitleClick = onHeaderTitleClick,
            connectionState = connectionState,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        DefaultMessageListHeaderTrailingContent(
            channel = channel,
            currentUser = currentUser,
            onClick = onChannelAvatarClick,
        )
    },
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        elevation = elevation,
        color = color,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()

            centerContent()

            trailingContent()
        }
    }
}

@Composable
internal fun DefaultMessageListHeaderLeadingContent(onBackPressed: () -> Unit) {
    val layoutDirection = LocalLayoutDirection.current

    BackButton(
        modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
        onBackPressed = onBackPressed,
    )
}

@Composable
public fun DefaultMessageListHeaderCenterContent(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    onHeaderTitleClick: (Channel) -> Unit = {},
) {
    val title = when (messageMode) {
        MessageMode.Normal -> ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
        is MessageMode.MessageThread -> stringResource(id = R.string.stream_compose_thread_title)
    }

    val subtitle = when (messageMode) {
        MessageMode.Normal -> channel.getMembersStatusText(LocalContext.current, currentUser)
        is MessageMode.MessageThread -> stringResource(
            R.string.stream_compose_thread_subtitle,
            ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
        )
    }

    Column(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onHeaderTitleClick(channel) },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )

        when (connectionState) {
            is ConnectionState.Connected -> {
                DefaultMessageListHeaderSubtitle(
                    subtitle = subtitle,
                    typingUsers = typingUsers,
                )
            }
            is ConnectionState.Connecting -> {
                NetworkLoadingIndicator(
                    modifier = Modifier.wrapContentHeight(),
                    spinnerSize = 12.dp,
                    textColor = ChatTheme.colors.textLowEmphasis,
                    textStyle = ChatTheme.typography.footnote,
                )
            }
            is ConnectionState.Offline -> {
                Text(
                    text = stringResource(id = R.string.stream_compose_disconnected),
                    color = ChatTheme.colors.textLowEmphasis,
                    style = ChatTheme.typography.footnote,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun DefaultMessageListHeaderSubtitle(
    subtitle: String,
    typingUsers: List<User>,
) {
    val textColor = ChatTheme.colors.textLowEmphasis
    val textStyle = ChatTheme.typography.footnote

    if (typingUsers.isEmpty()) {
        Text(
            text = subtitle,
            color = textColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val typingUsersText = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_header_typing_users,
                typingUsers.size,
                typingUsers.first().name,
                typingUsers.size - 1,
            )

            TypingIndicator()

            Text(
                text = typingUsersText,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun DefaultMessageListHeaderTrailingContent(
    channel: Channel,
    currentUser: User?,
    onClick: () -> Unit,
) {
    ChannelAvatar(
        modifier = Modifier.size(40.dp),
        channel = channel,
        currentUser = currentUser,
        contentDescription = channel.name,
        onClick = onClick,
    )
}

@Preview(name = "MessageListHeader Preview (Connected)")
@Composable
private fun MessageListHeaderConnectedPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Connecting)")
@Composable
private fun MessageListHeaderConnectingPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connecting,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Offline)")
@Composable
private fun MessageListHeaderOfflinePreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Offline,
        )
    }
}

@Preview(name = "MessageListHeader Preview (User Typing)")
@Composable
private fun MessageListHeaderUserTypingPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            typingUsers = listOf(PreviewUserData.user2),
            connectionState = ConnectionState.Connected,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Many Members)")
@Composable
private fun MessageListHeaderManyMembersPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithManyMembers,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
        )
    }
}