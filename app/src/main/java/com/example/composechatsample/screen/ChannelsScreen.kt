package com.example.composechatsample.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.ChannelListViewModel
import com.example.composechatsample.viewModel.ChannelViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Suppress("LongMethod")
fun ChannelsScreen(
    viewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    title: String = "Stream Chat",
    isShowingHeader: Boolean = true,
    searchMode: SearchMode = SearchMode.None,
    onHeaderActionClick: () -> Unit = {},
    onHeaderAvatarClick: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onSearchMessageItemClick: (Message) -> Unit = {},
    onViewChannelInfoAction: (Channel) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val listViewModel: ChannelListViewModel = viewModel(
        ChannelListViewModel::class.java,
        factory = viewModelFactory,
    )

    val selectedChannel by listViewModel.selectedChannel
    val user by listViewModel.user.collectAsState()
    val connectionState by listViewModel.connectionState.collectAsState()

    BackHandler(enabled = true) {
        if (selectedChannel != null) {
            listViewModel.selectChannel(null)
        } else {
            onBackPressed()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("Stream_ChannelsScreen"),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (isShowingHeader) {
                    ChannelListHeader(
                        onHeaderActionClick = onHeaderActionClick,
                        onAvatarClick = { onHeaderAvatarClick() },
                        currentUser = user,
                        title = title,
                        connectionState = connectionState,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(color = ChatTheme.colors.appBackground),
            ) {
                if (searchMode != SearchMode.None) {
                    SearchInput(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        query = searchQuery,
                        onSearchStarted = {},
                        onValueChange = remember(listViewModel) {
                            {
                                searchQuery = it
                                listViewModel.setSearchQuery(
                                    when {
                                        it.isBlank() -> SearchQuery.Empty
                                        searchMode == SearchMode.Channels -> SearchQuery.Channels(it)
                                        searchMode == SearchMode.Messages -> SearchQuery.Messages(it)
                                        else -> SearchQuery.Empty
                                    },
                                )
                            }
                        },
                    )
                }

                ChannelList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = listViewModel,
                    onChannelClick = onChannelClick,
                    onSearchResultClick = onSearchMessageItemClick,
                    onChannelLongClick = remember(listViewModel) {
                        {
                            listViewModel.selectChannel(it)
                        }
                    },
                )
            }
        }

        val channel = selectedChannel ?: Channel()
        AnimatedVisibility(
            visible = channel.cid.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
        ) {
            SelectedChannelMenu(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween(),
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2),
                        ),
                    ),
                selectedChannel = channel,
                currentUser = user,
                isMuted = listViewModel.isChannelMuted(channel.cid),
                onChannelOptionClick = remember(listViewModel) {
                    {
                            action ->
                        when (action) {
                            is ViewInfo -> onViewChannelInfoAction(action.channel)
                            is MuteChannel -> listViewModel.muteChannel(action.channel)
                            is UnmuteChannel -> listViewModel.unmuteChannel(action.channel)
                            else -> listViewModel.performChannelAction(action)
                        }
                    }
                },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }

        val activeAction = listViewModel.activeChannelAction

        if (activeAction is LeaveGroup) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_title,
                ),
                message = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_message,
                    ChatTheme.channelNameFormatter.formatChannelName(activeAction.channel, user),
                ),
                onPositiveAction = remember(listViewModel) { { listViewModel.leaveGroup(activeAction.channel) } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        } else if (activeAction is DeleteConversation) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_delete_conversation_confirmation_title,
                ),
                message = stringResource(
                    id = R.string.stream_compose_selected_channel_menu_delete_conversation_confirmation_message,
                    ChatTheme.channelNameFormatter.formatChannelName(activeAction.channel, user),
                ),
                onPositiveAction =
                remember(listViewModel) { { listViewModel.deleteConversation(activeAction.channel) } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }
    }
}

enum class SearchMode {
    None,
    Channels,
    Messages,
}