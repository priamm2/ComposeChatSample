package com.example.composechatsample.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.composechatsample.DeletedMessageVisibility
import com.example.composechatsample.R
import com.example.composechatsample.core.isDeleted
import com.example.composechatsample.core.isEmojiOnlyWithoutBubble
import com.example.composechatsample.core.isErrorOrFailed
import com.example.composechatsample.core.isGiphyEphemeral
import com.example.composechatsample.core.isThreadStart
import com.example.composechatsample.core.isUploading
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.ReactionSorting
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.components.MessageContent
import com.example.composechatsample.screen.components.MessageHeaderLabel
import com.example.composechatsample.screen.components.MessageText
import com.example.composechatsample.screen.components.UploadingFooter
import com.example.composechatsample.screen.components.UserAvatar
import com.example.composechatsample.screen.messages.MessageBubble
import com.example.composechatsample.screen.messages.MessageFocused
import com.example.composechatsample.screen.messages.MessageFooter
import com.example.composechatsample.screen.messages.MessagePosition
import com.example.composechatsample.screen.messages.MessageReactions
import com.example.composechatsample.screen.messages.OwnedMessageVisibilityContent
import com.example.composechatsample.screen.messages.QuotedMessage
import com.example.composechatsample.screen.messages.ReactionOptionItemState
import com.example.composechatsample.ui.theme.ChatTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemLeadingContent(messageItem = it)
    },
    headerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemHeaderContent(
            messageItem = it,
            reactionSorting = reactionSorting,
            onReactionsClick = onReactionsClick,
        )
    },
    centerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemCenterContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onGiphyActionClick = onGiphyActionClick,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
    footerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemFooterContent(messageItem = it)
    },
    trailingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemTrailingContent(messageItem = it)
    },
) {
    val message = messageItem.message
    val focusState = messageItem.focusState

    val clickModifier = if (message.isDeleted()) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { if (message.isThreadStart()) onThreadClick(message) },
            onLongClick = { if (!message.isUploading()) onLongItemClick(message) },
        )
    }

    val backgroundColor =
        if (focusState is MessageFocused || message.pinned) ChatTheme.colors.highlight else Color.Transparent
    val shouldAnimateBackground = !message.pinned && focusState != null

    val color = if (shouldAnimateBackground) {
        animateColorAsState(
            targetValue = backgroundColor,
            animationSpec = tween(
                durationMillis = if (focusState is MessageFocused) {
                    AnimationConstants.DefaultDurationMillis
                } else {
                    HighlightFadeOutDurationMillis
                },
            ),
        ).value
    } else {
        backgroundColor
    }

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val description = stringResource(id = R.string.stream_compose_cd_message_item)

    Box(
        modifier = Modifier
            .testTag("Stream_MessageItem")
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = color)
            .semantics { contentDescription = description },
        contentAlignment = messageAlignment.itemAlignment,
    ) {
        Row(
            modifier
                .widthIn(max = 300.dp)
                .then(clickModifier),
        ) {
            leadingContent(messageItem)

            Column(horizontalAlignment = messageAlignment.contentAlignment) {
                headerContent(messageItem)

                centerContent(messageItem)

                footerContent(messageItem)
            }

            trailingContent(messageItem)
        }
    }
}

@Composable
internal fun RowScope.DefaultMessageItemLeadingContent(
    messageItem: MessageItemState,
) {
    val modifier = Modifier
        .padding(start = 8.dp, end = 8.dp)
        .size(24.dp)
        .align(Alignment.Bottom)

    if (!messageItem.isMine &&
        (
            messageItem.showMessageFooter ||
                messageItem.groupPosition.contains(MessagePosition.BOTTOM) ||
                messageItem.groupPosition.contains(MessagePosition.NONE)
            )
    ) {
        UserAvatar(
            modifier = modifier,
            user = messageItem.message.user,
            textStyle = ChatTheme.typography.captionBold,
            showOnlineIndicator = false,
        )
    } else {
        Spacer(modifier = modifier)
    }
}

@Suppress("LongMethod")
@Composable
internal fun DefaultMessageItemHeaderContent(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onReactionsClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val currentUser = messageItem.currentUser

    if (message.pinned) {
        val pinnedByUser = if (message.pinnedBy?.id == currentUser?.id) {
            stringResource(id = R.string.stream_compose_message_list_you)
        } else {
            message.pinnedBy?.name
        }

        val pinnedByText = if (pinnedByUser != null) {
            stringResource(id = R.string.stream_compose_pinned_to_channel_by, pinnedByUser)
        } else {
            null
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_message_pinned),
            text = pinnedByText,
        )
    }

    if (message.showInChannel) {
        val alsoSendToChannelTextRes = if (messageItem.isInThread) {
            R.string.stream_compose_also_sent_to_channel
        } else {
            R.string.stream_compose_replied_to_thread
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread),
            text = stringResource(alsoSendToChannelTextRes),
        )
    }

    if (!message.isDeleted()) {
        val ownReactions = message.ownReactions
        val reactionGroups = message.reactionGroups.ifEmpty { return }
        val iconFactory = ChatTheme.reactionIconFactory
        reactionGroups
            .filter { iconFactory.isReactionSupported(it.key) }
            .takeIf { it.isNotEmpty() }
            ?.toList()
            ?.sortedWith { o1, o2 -> reactionSorting.compare(o1.second, o2.second) }
            ?.map { (type, _) ->
                val isSelected = ownReactions.any { it.type == type }
                val reactionIcon = iconFactory.createReactionIcon(type)
                ReactionOptionItemState(
                    painter = reactionIcon.getPainter(isSelected),
                    type = type,
                )
            }
            ?.let { options ->
                MessageReactions(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false),
                        ) {
                            onReactionsClick(message)
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    options = options,
                )
            }
    }
}

@Composable
internal fun ColumnScope.DefaultMessageItemFooterContent(
    messageItem: MessageItemState,
) {
    val message = messageItem.message
    when {
        message.isUploading() -> {
            UploadingFooter(
                modifier = Modifier.align(End),
                message = message,
            )
        }
        message.isDeleted() &&
            messageItem.deletedMessageVisibility == DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
            OwnedMessageVisibilityContent(message = message)
        }
        else -> {
            MessageFooter(messageItem = messageItem)
        }
    }

    val position = messageItem.groupPosition
    val spacerSize =
        if (position.contains(MessagePosition.NONE) || position.contains(MessagePosition.BOTTOM)) 4.dp else 2.dp

    Spacer(Modifier.size(spacerSize))
}

@Composable
internal fun DefaultMessageItemTrailingContent(
    messageItem: MessageItemState,
) {
    if (messageItem.isMine) {
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
internal fun DefaultMessageItemCenterContent(
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val modifier = Modifier.widthIn(max = ChatTheme.dimens.messageItemMaxWidth)
    if (messageItem.message.isEmojiOnlyWithoutBubble()) {
        EmojiMessageContent(
            modifier = modifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        RegularMessageContent(
            modifier = modifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    }
}

@Composable
internal fun EmojiMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message

    if (!messageItem.isErrorOrFailed()) {
        MessageContent(
            message = message,
            currentUser = messageItem.currentUser,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        Box(modifier = modifier) {
            MessageContent(
                message = message,
                currentUser = messageItem.currentUser,
                onLongItemClick = onLongItemClick,
                onGiphyActionClick = onGiphyActionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onQuotedMessageClick = onQuotedMessageClick,
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

@Composable
internal fun RegularMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageBubbleShape = when {
        position.contains(MessagePosition.TOP) || position.contains(MessagePosition.MIDDLE) -> RoundedCornerShape(16.dp)
        else -> {
            if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        }
    }

    val messageBubbleColor = when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.deletedBackgroundColor
            else -> ChatTheme.otherMessageTheme.deletedBackgroundColor
        }

        else -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.backgroundColor
            else -> ChatTheme.otherMessageTheme.backgroundColor
        }
    }

    if (!messageItem.isErrorOrFailed()) {
        MessageBubble(
            modifier = modifier,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = if (messageItem.isMine) null else BorderStroke(1.dp, ChatTheme.colors.borders),
            content = {
                MessageContent(
                    message = message,
                    currentUser = messageItem.currentUser,
                    onLongItemClick = onLongItemClick,
                    onGiphyActionClick = onGiphyActionClick,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onQuotedMessageClick = onQuotedMessageClick,
                )
            },
        )
    } else {
        Box(modifier = modifier) {
            MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                shape = messageBubbleShape,
                color = messageBubbleColor,
                content = {
                    MessageContent(
                        message = message,
                        currentUser = messageItem.currentUser,
                        onLongItemClick = onLongItemClick,
                        onGiphyActionClick = onGiphyActionClick,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onQuotedMessageClick = onQuotedMessageClick,
                    )
                },
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

@Composable
internal fun DefaultMessageTextContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
) {
    val quotedMessage = message.replyTo

    Column {
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                message = quotedMessage,
                currentUser = currentUser,
                replyMessage = message,
                onLongItemClick = { onLongItemClick(message) },
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
        MessageText(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
        )
    }
}

const val HighlightFadeOutDurationMillis: Int = 1000