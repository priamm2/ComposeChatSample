package com.example.composechatsample.screen.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageReactions(
    options: List<ReactionOptionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        MessageReactionItem(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp)
                .align(Alignment.CenterVertically),
            option = option,
        )
    },
) {
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            itemContent(option)
        }
    }
}

@Preview
@Composable
private fun OneMessageReactionPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionOptionData.oneReaction())
    }
}

@Preview
@Composable
private fun ManyMessageReactionsPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionOptionData.manyReactions())
    }
}