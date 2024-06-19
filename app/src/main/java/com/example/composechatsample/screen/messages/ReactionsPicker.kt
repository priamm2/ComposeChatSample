package com.example.composechatsample.screen.messages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.screen.PreviewMessageData
import com.example.composechatsample.screen.components.SimpleMenu
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ReactionIcon

private const val DefaultNumberOfReactions = 5

@Composable
public fun ReactionsPicker(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    cells: GridCells = GridCells.Fixed(DefaultNumberOfReactions),
    onDismiss: () -> Unit = {},
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    headerContent: @Composable ColumnScope.() -> Unit = {},
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultReactionsPickerCenterContent(
            message = message,
            onMessageAction = onMessageAction,
            cells = cells,
            reactionTypes = reactionTypes,
        )
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        headerContent = headerContent,
        centerContent = centerContent,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DefaultReactionsPickerCenterContent(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    cells: GridCells = GridCells.Fixed(DefaultNumberOfReactions),
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
) {
    ExtendedReactionsOptions(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        reactionTypes = reactionTypes,
        ownReactions = message.ownReactions,
        onReactionOptionSelected = { reactionOptionItemState ->
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, reactionOptionItemState.type),
                    message = message,
                ),
            )
        },
        cells = cells,
    )
}

@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ReactionPicker Preview")
@Composable
internal fun ReactionPickerPreview() {
    ChatTheme {
        ReactionsPicker(
            message = PreviewMessageData.messageWithOwnReaction,
            onMessageAction = {},
        )
    }
}