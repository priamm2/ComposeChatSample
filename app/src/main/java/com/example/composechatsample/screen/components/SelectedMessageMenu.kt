package com.example.composechatsample.screen.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.toSet
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.MessageOptionItemState
import com.example.composechatsample.screen.messages.MessageOptions
import com.example.composechatsample.screen.messages.React
import com.example.composechatsample.screen.messages.ReactionOptions
import com.example.composechatsample.screen.messages.defaultMessageOptionsState
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ReactionIcon

@Composable
public fun SelectedMessageMenu(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
    ownCapabilities: Set<String>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        val canLeaveReaction = ownCapabilities.contains(ChannelCapabilities.SEND_REACTION)

        if (canLeaveReaction) {
            DefaultSelectedMessageReactionOptions(
                message = message,
                reactionTypes = reactionTypes,
                showMoreReactionsDrawableRes = showMoreReactionsIcon,
                onMessageAction = onMessageAction,
                showMoreReactionsIcon = onShowMoreReactionsSelected,
            )
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedMessageOptions(
            messageOptions = messageOptions,
            onMessageAction = onMessageAction,
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
internal fun DefaultSelectedMessageReactionOptions(
    message: Message,
    reactionTypes: Map<String, ReactionIcon>,
    @DrawableRes showMoreReactionsDrawableRes: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    showMoreReactionsIcon: () -> Unit,
) {
    ReactionOptions(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
        reactionTypes = reactionTypes,
        showMoreReactionsIcon = showMoreReactionsDrawableRes,
        onReactionOptionSelected = {
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, type = it.type),
                    message = message,
                ),
            )
        },
        onShowMoreReactionsSelected = showMoreReactionsIcon,
        ownReactions = message.ownReactions,
    )
}

@Composable
internal fun DefaultSelectedMessageOptions(
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
) {
    MessageOptions(
        options = messageOptions,
        onMessageOptionSelected = {
            onMessageAction(it.action)
        },
    )
}

@Preview(showBackground = true, name = "SelectedMessageMenu Preview")
@Composable
private fun SelectedMessageMenuPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = Message(),
            currentUser = User(),
            isInThread = false,
            ownCapabilities = ChannelCapabilities.toSet(),
        )

        SelectedMessageMenu(
            message = Message(),
            messageOptions = messageOptionsStateList,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}