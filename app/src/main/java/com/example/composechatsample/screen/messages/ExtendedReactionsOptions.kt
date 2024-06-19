package com.example.composechatsample.screen.messages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ReactionIcon
import androidx.compose.runtime.key

private const val DefaultNumberOfColumns = 5

@ExperimentalFoundationApi
@Composable
public fun ExtendedReactionsOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    cells: GridCells = GridCells.Fixed(DefaultNumberOfColumns),
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    itemContent: @Composable LazyGridScope.(ReactionOptionItemState) -> Unit = { option ->
        DefaultExtendedReactionsItemContent(
            option = option,
            onReactionOptionSelected = onReactionOptionSelected,
        )
    },
) {
    val options = reactionTypes.entries.map { (type, reactionIcon) ->
        val isSelected = ownReactions.any { ownReaction -> ownReaction.type == type }
        ReactionOptionItemState(
            painter = reactionIcon.getPainter(isSelected),
            type = type,
        )
    }

    LazyVerticalGrid(modifier = modifier, columns = cells) {
        items(options) { item ->
            key(item.type) {
                this@LazyVerticalGrid.itemContent(item)
            }
        }
    }
}

@Composable
internal fun DefaultExtendedReactionsItemContent(
    option: ReactionOptionItemState,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
) {
    ReactionOptionItem(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionOptionSelected(option) },
            ),
        option = option,
    )
}

@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview")
@Composable
internal fun ExtendedReactionOptionsPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(),
            onReactionOptionSelected = {},
        )
    }
}

/**
 * Preview for [ExtendedReactionsOptions] with a selected reaction.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview (With Own Reaction)")
@Composable
internal fun ExtendedReactionOptionsWithOwnReactionPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(
                Reaction(
                    messageId = "messageId",
                    type = "haha",
                ),
            ),
            onReactionOptionSelected = {},
        )
    }
}