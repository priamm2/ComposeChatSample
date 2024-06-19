package com.example.composechatsample.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.Reply
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageInputOptions(
    activeAction: MessageAction,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val optionImage =
        painterResource(
            id = if (activeAction is Reply) R.drawable.stream_compose_ic_reply else R.drawable.stream_compose_ic_edit,
        )
    val title = stringResource(
        id = if (activeAction is Reply) {
            R.string.stream_compose_reply_to_message
        } else {
            R.string.stream_compose_edit_message
        },
    )

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            painter = optionImage,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Icon(
            modifier = Modifier
                .padding(4.dp)
                .clickable(
                    onClick = onCancelAction,
                    indication = rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_close),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }
}