package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun ReactionOptionItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = option.painter,
        contentDescription = option.type,
    )
}

@Preview(showBackground = true, name = "ReactionOptionItem Preview (Not Selected)")
@Composable
private fun ReactionOptionItemNotSelectedPreview() {
    ChatTheme {
        ReactionOptionItem(option = PreviewReactionOptionData.reactionOption1())
    }
}

/**
 * Preview of [ReactionOptionItem] in its selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Selected)")
@Composable
private fun ReactionOptionItemSelectedPreview() {
    ChatTheme {
        ReactionOptionItem(option = PreviewReactionOptionData.reactionOption2())
    }
}