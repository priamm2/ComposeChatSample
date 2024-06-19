package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageReactionItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = option.painter,
        contentDescription = null,
    )
}

@Preview
@Composable
public fun MessageReactionItemSelectedPreview() {
    ChatTheme {
        MessageReactionItem(option = PreviewReactionOptionData.reactionOption2())
    }
}

@Preview
@Composable
public fun MessageReactionItemNotSelectedPreview() {
    ChatTheme {
        MessageReactionItem(option = PreviewReactionOptionData.reactionOption1())
    }
}