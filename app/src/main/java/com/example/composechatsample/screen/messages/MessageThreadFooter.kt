package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.MessageAlignment

@Composable
public fun MessageThreadFooter(
    participants: List<User>,
    text: String,
    messageAlignment: MessageAlignment,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = 4.dp)) {
        if (messageAlignment == MessageAlignment.Start) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(end = 4.dp),
                participants = participants,
                alignment = messageAlignment,
            )
        }

        Text(
            text = text,
            style = ChatTheme.typography.footnoteBold,
            color = ChatTheme.colors.primaryAccent,
        )

        if (messageAlignment == MessageAlignment.End) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(start = 4.dp),
                participants = participants,
                alignment = messageAlignment,
            )
        }
    }
}