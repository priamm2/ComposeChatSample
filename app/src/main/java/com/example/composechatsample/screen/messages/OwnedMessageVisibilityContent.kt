package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.components.Timestamp
import com.example.composechatsample.ui.theme.ChatTheme
import java.util.Date

@Composable
public fun OwnedMessageVisibilityContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(12.dp),
            painter = painterResource(id = R.drawable.stream_compose_ic_visible_to_you),
            contentDescription = null,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_only_visible_to_you),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Timestamp(
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
            date = message.updatedAt ?: message.createdAt ?: Date(),
        )
    }
}