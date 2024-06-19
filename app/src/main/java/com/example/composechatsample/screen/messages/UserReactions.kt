package com.example.composechatsample.screen.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.PreviewUserReactionData
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun UserReactions(
    items: List<UserReactionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (UserReactionItemState) -> Unit = {
        DefaultUserReactionItem(item = it)
    },
) {
    val reactionCount = items.size

    val reactionCountText = LocalContext.current.resources.getQuantityString(
        R.plurals.stream_compose_message_reactions,
        reactionCount,
        reactionCount,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(ChatTheme.colors.barsBackground),
    ) {
        Text(
            text = reactionCountText,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reactionCount > 0) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val reactionItemWidth = ChatTheme.dimens.userReactionItemWidth
                val maxColumns = maxOf((maxWidth / reactionItemWidth).toInt(), 1)
                val columns = reactionCount.coerceAtMost(maxColumns)
                val reactionGridWidth = reactionItemWidth * columns

                LazyVerticalGrid(
                    modifier = Modifier
                        .width(reactionGridWidth)
                        .align(Alignment.Center),
                    columns = GridCells.Fixed(columns),
                ) {
                    items(reactionCount) { index ->
                        itemContent(items[index])
                    }
                }
            }
        }
    }
}

@Composable
internal fun DefaultUserReactionItem(item: UserReactionItemState) {
    UserReactionItem(
        item = item,
        modifier = Modifier,
    )
}

@Preview
@Composable
private fun OneUserReactionPreview() {
    ChatTheme {
        UserReactions(items = PreviewUserReactionData.oneUserReaction())
    }
}

@Preview
@Composable
private fun ManyUserReactionsPreview() {
    ChatTheme {
        UserReactions(items = PreviewUserReactionData.manyUserReactions())
    }
}