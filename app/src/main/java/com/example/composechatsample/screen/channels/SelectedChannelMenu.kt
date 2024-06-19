package com.example.composechatsample.screen.channels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.getMembersStatusText
import com.example.composechatsample.core.isOneToOne
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.PreviewChannelData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.components.SimpleMenu
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun SelectedChannelMenu(
    selectedChannel: Channel,
    isMuted: Boolean,
    currentUser: User?,
    onChannelOptionClick: (ChannelAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    channelOptions: List<ChannelOptionState> = buildDefaultChannelOptionsState(
        selectedChannel = selectedChannel,
        isMuted = isMuted,
        ownCapabilities = selectedChannel.ownCapabilities,
    ),
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    headerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedChannelMenuHeaderContent(
            selectedChannel = selectedChannel,
            currentUser = currentUser,
        )
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedChannelMenuCenterContent(
            onChannelOptionClick = onChannelOptionClick,
            channelOptions = channelOptions,
        )
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        onDismiss = onDismiss,
        headerContent = headerContent,
        centerContent = centerContent,
    )
}

@Composable
internal fun DefaultSelectedChannelMenuHeaderContent(
    selectedChannel: Channel,
    currentUser: User?,
) {
    val channelMembers = selectedChannel.members
    val membersToDisplay = if (selectedChannel.isOneToOne(currentUser)) {
        channelMembers.filter { it.user.id != currentUser?.id }
    } else {
        channelMembers
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        textAlign = TextAlign.Center,
        text = ChatTheme.channelNameFormatter.formatChannelName(selectedChannel, currentUser),
        style = ChatTheme.typography.title3Bold,
        color = ChatTheme.colors.textHighEmphasis,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = selectedChannel.getMembersStatusText(LocalContext.current, currentUser),
        style = ChatTheme.typography.footnoteBold,
        color = ChatTheme.colors.textLowEmphasis,
    )

    ChannelMembers(membersToDisplay)
}

@Composable
internal fun DefaultSelectedChannelMenuCenterContent(
    onChannelOptionClick: (ChannelAction) -> Unit,
    channelOptions: List<ChannelOptionState>,
) {
    ChannelOptions(channelOptions, onChannelOptionClick)
}

@Preview(showBackground = true, name = "SelectedChannelMenu Preview (Centered dialog)")
@Composable
private fun SelectedChannelMenuCenteredDialogPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SelectedChannelMenu(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                selectedChannel = PreviewChannelData.channelWithManyMembers,
                isMuted = false,
                currentUser = PreviewUserData.user1,
                onChannelOptionClick = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "SelectedChannelMenu Preview (Bottom sheet dialog)")
@Composable
private fun SelectedChannelMenuBottomSheetDialogPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SelectedChannelMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                shape = ChatTheme.shapes.bottomSheet,
                selectedChannel = PreviewChannelData.channelWithManyMembers,
                isMuted = false,
                currentUser = PreviewUserData.user1,
                onChannelOptionClick = {},
                onDismiss = {},
            )
        }
    }
}