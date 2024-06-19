package com.example.composechatsample.screen.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.example.composechatsample.screen.PreviewReactionData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.React
import com.example.composechatsample.screen.messages.ReactionOptions
import com.example.composechatsample.screen.messages.UserReactionItemState
import com.example.composechatsample.screen.messages.UserReactions
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ReactionIcon

@Composable
public fun SelectedReactionsMenu(
    message: Message,
    currentUser: User?,
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
            DefaultSelectedReactionsHeaderContent(
                message = message,
                reactionTypes = reactionTypes,
                showMoreReactionsIcon = showMoreReactionsIcon,
                onMessageAction = onMessageAction,
                onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            )
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedReactionsCenterContent(
            message = message,
            currentUser = currentUser,
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
internal fun DefaultSelectedReactionsHeaderContent(
    message: Message,
    reactionTypes: Map<String, ReactionIcon>,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
) {
    ReactionOptions(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
        reactionTypes = reactionTypes,
        showMoreReactionsIcon = showMoreReactionsIcon,
        onReactionOptionSelected = {
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, type = it.type),
                    message = message,
                ),
            )
        },
        onShowMoreReactionsSelected = onShowMoreReactionsSelected,
        ownReactions = message.ownReactions,
    )
}

@Composable
internal fun DefaultSelectedReactionsCenterContent(
    message: Message,
    currentUser: User?,
) {
    UserReactions(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.userReactionsMaxHeight)
            .padding(vertical = 16.dp),
        items = buildUserReactionItems(
            message = message,
            currentUser = currentUser,
        ),
    )
}

@Composable
private fun buildUserReactionItems(
    message: Message,
    currentUser: User?,
): List<UserReactionItemState> {
    val iconFactory = ChatTheme.reactionIconFactory
    return message.latestReactions
        .filter { it.user != null && iconFactory.isReactionSupported(it.type) }
        .map {
            val user = requireNotNull(it.user)
            val type = it.type
            val isMine = currentUser?.id == user.id
            val painter = iconFactory.createReactionIcon(type).getPainter(isMine)

            UserReactionItemState(
                user = user,
                painter = painter,
                type = type,
            )
        }
}

@Preview
@Composable
private fun OneSelectedReactionMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.oneReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}

@Preview
@Composable
private fun ManySelectedReactionsMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.manyReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {},
            ownCapabilities = ChannelCapabilities.toSet(),
        )
    }
}