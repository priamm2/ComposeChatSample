package com.example.composechatsample.screen.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
fun ChannelListHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    currentUser: User? = null,
    connectionState: ConnectionState,
    color: Color = ChatTheme.colors.barsBackground,
    shape: Shape = ChatTheme.shapes.header,
    elevation: Dp = ChatTheme.dimens.headerElevation,
    onAvatarClick: (User?) -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        DefaultChannelHeaderLeadingContent(
            currentUser = currentUser,
            onAvatarClick = onAvatarClick,
        )
    },
    centerContent: @Composable RowScope.() -> Unit = {
        DefaultChannelListHeaderCenterContent(
            connectionState = connectionState,
            title = title,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        DefaultChannelListHeaderTrailingContent(
            onHeaderActionClick = onHeaderActionClick,
        )
    },
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        elevation = elevation,
        color = color,
        shape = shape,
    ) {
        Row(
            Modifier
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
internal fun DefaultChannelHeaderLeadingContent(
    currentUser: User?,
    onAvatarClick: (User?) -> Unit,
) {
    val size = Modifier.size(40.dp)

    if (currentUser != null) {
        UserAvatar(
            modifier = size,
            user = currentUser,
            contentDescription = currentUser.name,
            showOnlineIndicator = false,
            onClick = { onAvatarClick(currentUser) },
        )
    } else {
        Spacer(modifier = size)
    }
}

@Composable
internal fun RowScope.DefaultChannelListHeaderCenterContent(
    connectionState: ConnectionState,
    title: String,
) {
    when (connectionState) {
        is ConnectionState.Connected -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp),
                text = title,
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
        is ConnectionState.Connecting -> NetworkLoadingIndicator(modifier = Modifier.weight(1f))
        is ConnectionState.Offline -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.stream_compose_disconnected),
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DefaultChannelListHeaderTrailingContent(
    onHeaderActionClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        onClick = onHeaderActionClick,
        interactionSource = remember { MutableInteractionSource() },
        color = ChatTheme.colors.primaryAccent,
        shape = ChatTheme.shapes.avatar,
        elevation = 4.dp,
    ) {
        Icon(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = R.drawable.stream_compose_ic_new_chat),
            contentDescription = stringResource(id = R.string.stream_compose_channel_list_header_new_chat),
            tint = Color.White,
        )
    }
}

@Preview(name = "ChannelListHeader Preview (Connected state)")
@Composable
private fun ChannelListHeaderForConnectedStatePreview() {
    ChannelListHeaderPreview(connectionState = ConnectionState.Connected)
}

@Preview(name = "ChannelListHeader Preview (Connecting state)")
@Composable
private fun ChannelListHeaderForConnectingStatePreview() {
    ChannelListHeaderPreview(connectionState = ConnectionState.Connecting)
}

@Composable
private fun ChannelListHeaderPreview(
    title: String = "Stream Chat",
    currentUser: User? = PreviewUserData.user1,
    connectionState: ConnectionState = ConnectionState.Connected,
) {
    ChatTheme {
        ChannelListHeader(
            title = title,
            currentUser = currentUser,
            connectionState = connectionState,
        )
    }
}