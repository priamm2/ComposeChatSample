package com.example.composechatsample.screen.messages

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ReactionIcon

@Composable
public fun ReactionOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    numberOfReactionsShown: Int = DefaultNumberOfReactionsShown,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    reactionTypes: Map<String, ReactionIcon> = ChatTheme.reactionIconFactory.createReactionIcons(),
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        DefaultReactionOptionItem(
            option = option,
            onReactionOptionSelected = onReactionOptionSelected,
        )
    },
) {
    val options = reactionTypes.entries.map { (type, reactionIcon) ->
        val isSelected = ownReactions.any { ownReaction -> ownReaction.type == type }
        val painter = reactionIcon.getPainter(isSelected)
        ReactionOptionItemState(
            painter = painter,
            type = type,
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
    ) {
        options.take(numberOfReactionsShown).forEach { option ->
            key(option.type) {
                itemContent(option)
            }
        }

        if (options.size > numberOfReactionsShown) {
            Icon(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { onShowMoreReactionsSelected() },
                ),
                painter = painterResource(id = showMoreReactionsIcon),
                contentDescription = LocalContext.current.getString(R.string.stream_compose_show_more_reactions),
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

@Composable
internal fun DefaultReactionOptionItem(
    option: ReactionOptionItemState,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
) {
    ReactionOptionItem(
        modifier = Modifier
            .size(24.dp)
            .size(ChatTheme.dimens.reactionOptionItemIconSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionOptionSelected(option) },
            ),
        option = option,
    )
}

@Preview(showBackground = true, name = "ReactionOptions Preview")
@Composable
private fun ReactionOptionsPreview() {
    ChatTheme {
        val reactionType = ChatTheme.reactionIconFactory
            .createReactionIcons()
            .keys
            .firstOrNull()

        if (reactionType != null) {
            ReactionOptions(
                ownReactions = listOf(Reaction(reactionType)),
                onReactionOptionSelected = {},
                onShowMoreReactionsSelected = {},
            )
        }
    }
}

private const val DefaultNumberOfReactionsShown = 5