package com.example.composechatsample.screen.messages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composechatsample.R
import com.example.composechatsample.StatefulStreamMediaRecorder
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.ReactionSorting
import com.example.composechatsample.core.models.ReactionSortingByFirstReactionAt
import com.example.composechatsample.screen.MediaGalleryPreviewResultType
import com.example.composechatsample.screen.MessageList
import com.example.composechatsample.screen.components.SelectedMessageMenu
import com.example.composechatsample.screen.components.SelectedReactionsMenu
import com.example.composechatsample.screen.components.SimpleDialog
import com.example.composechatsample.screen.components.rememberMessageListState
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ThreadMessagesStart
import com.example.composechatsample.viewModel.AttachmentsPickerViewModel
import com.example.composechatsample.viewModel.MessageComposerViewModel
import com.example.composechatsample.viewModel.MessageListViewModel
import com.example.composechatsample.viewModel.MessagesViewModelFactory
import com.example.composechatsample.screen.messages.DeleteMessage

@Composable
public fun MessagesScreen(
    viewModelFactory: MessagesViewModelFactory,
    showHeader: Boolean = true,
    reactionSorting: ReactionSorting = ReactionSortingByFirstReactionAt,
    onBackPressed: () -> Unit = {},
    onHeaderTitleClick: (channel: Channel) -> Unit = {},
    onChannelAvatarClick: () -> Unit = {},
    onComposerLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    skipPushNotification: Boolean = false,
    skipEnrichUrl: Boolean = false,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder? = null,
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    val messageMode = listViewModel.messageMode

    if (messageMode is MessageMode.MessageThread) {
        composerViewModel.setMessageMode(messageMode)
    }

    val backAction = remember(listViewModel, composerViewModel, attachmentsPickerViewModel) {
        {
            val isInThread = listViewModel.isInThread
            val isShowingOverlay = listViewModel.isShowingOverlay

            when {
                attachmentsPickerViewModel.isShowingAttachments -> attachmentsPickerViewModel.changeAttachmentState(
                    false,
                )

                isShowingOverlay -> listViewModel.selectMessage(null)
                isInThread -> {
                    listViewModel.leaveThread()
                    composerViewModel.leaveThread()
                }

                else -> onBackPressed()
            }
        }
    }

    BackHandler(enabled = true, onBack = backAction)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("Stream_MessagesScreen"),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showHeader) {
                    val connectionState by listViewModel.connectionState.collectAsState()
                    val user by listViewModel.user.collectAsState()

                    MessageListHeader(
                        modifier = Modifier
                            .height(56.dp),
                        channel = listViewModel.channel,
                        currentUser = user,
                        typingUsers = listViewModel.typingUsers,
                        connectionState = connectionState,
                        messageMode = messageMode,
                        onBackPressed = backAction,
                        onHeaderTitleClick = onHeaderTitleClick,
                        onChannelAvatarClick = onChannelAvatarClick,
                    )
                }
            },
            bottomBar = {
                MessageComposer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    viewModel = composerViewModel,
                    onAttachmentsClick = remember(attachmentsPickerViewModel) {
                        {
                            attachmentsPickerViewModel.changeAttachmentState(
                                true,
                            )
                        }
                    },
                    onCommandsClick = remember(composerViewModel) { { composerViewModel.toggleCommandsVisibility() } },
                    onCancelAction = remember(listViewModel, composerViewModel) {
                        {
                            listViewModel.dismissAllMessageActions()
                            composerViewModel.dismissMessageActions()
                        }
                    },
                    onLinkPreviewClick = onComposerLinkPreviewClick,
                    onSendMessage = remember(composerViewModel) {
                        {
                                message ->
                            composerViewModel.sendMessage(
                                message.copy(
                                    skipPushNotification = skipPushNotification,
                                    skipEnrichUrl = skipEnrichUrl,
                                ),
                            )
                        }
                    },
                    statefulStreamMediaRecorder = statefulStreamMediaRecorder,
                )
            },
        ) {
            val currentState = listViewModel.currentMessagesState

            MessageList(
                modifier = Modifier
                    .testTag("Stream_MessagesList")
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(it),
                viewModel = listViewModel,
                reactionSorting = reactionSorting,
                messagesLazyListState = rememberMessageListState(parentMessageId = currentState.parentMessageId),
                threadMessagesStart = threadMessagesStart,
                onThreadClick = remember(composerViewModel, listViewModel) {
                    {
                            message ->
                        composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                        listViewModel.openMessageThread(message)
                    }
                },
                onMediaGalleryPreviewResult = remember(listViewModel, composerViewModel) {
                    {
                            result ->
                        when (result?.resultType) {
                            MediaGalleryPreviewResultType.QUOTE -> {
                                val message = listViewModel.getMessageById(result.messageId)

                                if (message != null) {
                                    composerViewModel.performMessageAction(
                                        Reply(
                                            message.copy(
                                                skipPushNotification = skipPushNotification,
                                                skipEnrichUrl = skipEnrichUrl,
                                            ),
                                        ),
                                    )
                                }
                            }

                            MediaGalleryPreviewResultType.SHOW_IN_CHAT -> {
                                listViewModel.scrollToMessage(
                                    messageId = result.messageId,
                                    parentMessageId = result.parentMessageId,
                                )
                            }

                            null -> Unit
                        }
                    }
                },
            )
        }

        MessageMenus(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        AttachmentsPickerMenu(
            attachmentsPickerViewModel = attachmentsPickerViewModel,
            composerViewModel = composerViewModel,
        )
        MessageModerationDialog(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        MessageDialogs(listViewModel = listViewModel)
    }
}

@Composable
private fun BoxScope.MessageMenus(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState

    val selectedMessage = selectedMessageState?.message ?: Message()

    MessagesScreenMenus(
        listViewModel = listViewModel,
        composerViewModel = composerViewModel,
        selectedMessageState = selectedMessageState,
        selectedMessage = selectedMessage,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
    )

    MessagesScreenReactionsPicker(
        listViewModel = listViewModel,
        composerViewModel = composerViewModel,
        selectedMessageState = selectedMessageState,
        selectedMessage = selectedMessage,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.MessagesScreenMenus(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    selectedMessageState: SelectedMessageState?,
    selectedMessage: Message,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val user by listViewModel.user.collectAsState()

    val ownCapabilities = selectedMessageState?.ownCapabilities ?: setOf()

    val isInThread = listViewModel.isInThread

    val newMessageOptions = defaultMessageOptionsState(
        selectedMessage = selectedMessage,
        currentUser = user,
        isInThread = isInThread,
        ownCapabilities = ownCapabilities,
    )

    var messageOptions by remember {
        mutableStateOf<List<MessageOptionItemState>>(emptyList())
    }

    if (newMessageOptions.isNotEmpty()) {
        messageOptions = newMessageOptions
    }

    AnimatedVisibility(
        visible = selectedMessageState is SelectedMessageOptionsState && selectedMessage.id.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        SelectedMessageMenu(
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
            messageOptions = messageOptions,
            message = selectedMessage,
            ownCapabilities = ownCapabilities,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(it)
                        listViewModel.performMessageAction(it)
                    }
                }
            },
            onShowMoreReactionsSelected = remember(listViewModel) {
                {
                    listViewModel.selectExtendedReactions(selectedMessage)
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
        )
    }

    AnimatedVisibility(
        visible = selectedMessageState is SelectedMessageReactionsState && selectedMessage.id.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        SelectedReactionsMenu(
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
            currentUser = user,
            message = selectedMessage,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(it)
                        listViewModel.performMessageAction(it)
                    }
                }
            },
            onShowMoreReactionsSelected = remember(listViewModel) {
                {
                    listViewModel.selectExtendedReactions(selectedMessage)
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
            ownCapabilities = selectedMessageState?.ownCapabilities ?: setOf(),
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.MessagesScreenReactionsPicker(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    selectedMessageState: SelectedMessageState?,
    selectedMessage: Message,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    AnimatedVisibility(
        visible = selectedMessageState is SelectedMessageReactionsPickerState && selectedMessage.id.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        ReactionsPicker(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(max = 400.dp)
                .wrapContentHeight()
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
            message = selectedMessage,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(action)
                        listViewModel.performMessageAction(action)
                    }
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.AttachmentsPickerMenu(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    composerViewModel: MessageComposerViewModel,
) {
    val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

    AnimatedVisibility(
        visible = isShowingAttachments,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(delayMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        AttachmentsPicker(
            attachmentsPickerViewModel = attachmentsPickerViewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(350.dp)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween(),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(delayMillis = AnimationConstants.DefaultDurationMillis / 2),
                    ),
                ),
            onAttachmentsSelected = remember(attachmentsPickerViewModel) {
                {
                        attachments ->
                    attachmentsPickerViewModel.changeAttachmentState(false)
                    composerViewModel.addSelectedAttachments(attachments)
                }
            },
            onDismiss = remember(attachmentsPickerViewModel) {
                {
                    attachmentsPickerViewModel.changeAttachmentState(false)
                    attachmentsPickerViewModel.dismissAttachments()
                }
            },
        )
    }
}

@Composable
private fun MessageModerationDialog(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState

    val selectedMessage = selectedMessageState?.message ?: Message()

    if (selectedMessageState is SelectedMessageFailedModerationState) {
        ModeratedMessageDialog(
            message = selectedMessage,
            modifier = Modifier.background(
                shape = MaterialTheme.shapes.medium,
                color = ChatTheme.colors.inputBackground,
            ),
            onDismissRequest = remember(listViewModel) { { listViewModel.removeOverlay() } },
            onDialogOptionInteraction = remember(listViewModel, composerViewModel) {
                {
                        message, action ->
                    when (action) {
                        DeleteMessage -> listViewModel.deleteMessage(message = message, true)
                        EditMessage -> composerViewModel.performMessageAction(Edit(message))
                        SendAnyway -> listViewModel.performMessageAction(
                            Resend(
                                message.copy(
                                    skipPushNotification = skipPushNotification,
                                    skipEnrichUrl = skipEnrichUrl,
                                ),
                            ),
                        )

                        else -> {
                            // Custom events
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun MessageDialogs(listViewModel: MessageListViewModel) {
    val messageActions = listViewModel.messageActions

    val deleteAction = messageActions.firstOrNull { it is Delete }

    if (deleteAction != null) {
        SimpleDialog(
            modifier = Modifier.padding(16.dp),
            title = stringResource(id = R.string.stream_compose_delete_message_title),
            message = stringResource(id = R.string.stream_compose_delete_message_text),
            onPositiveAction = remember(listViewModel) { { listViewModel.deleteMessage(deleteAction.message) } },
            onDismiss = remember(listViewModel) { { listViewModel.dismissMessageAction(deleteAction) } },
        )
    }

    val flagAction = messageActions.firstOrNull { it is Flag }

    if (flagAction != null) {
        SimpleDialog(
            modifier = Modifier.padding(16.dp),
            title = stringResource(id = R.string.stream_compose_flag_message_title),
            message = stringResource(id = R.string.stream_compose_flag_message_text),
            onPositiveAction = remember(listViewModel) {
                {
                    listViewModel.flagMessage(
                        flagAction.message,
                        reason = null,
                        customData = emptyMap(),
                    )
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.dismissMessageAction(flagAction) } },
        )
    }
}