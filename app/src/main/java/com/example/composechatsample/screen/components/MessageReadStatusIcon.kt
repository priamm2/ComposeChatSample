package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.getCreatedAtOrThrow
import com.example.composechatsample.core.getReadStatuses
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.PreviewMessageData
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageReadStatusIcon(
    channel: Channel,
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val readStatues = channel.getReadStatuses(userToIgnore = currentUser)
    val readCount = readStatues.count { it.time >= message.getCreatedAtOrThrow().time }
    val isMessageRead = readCount != 0

    MessageReadStatusIcon(
        message = message,
        isMessageRead = isMessageRead,
        modifier = modifier,
        readCount = readCount,
    )
}

@Composable
public fun MessageReadStatusIcon(
    message: Message,
    isMessageRead: Boolean,
    modifier: Modifier = Modifier,
    readCount: Int = 0,
) {
    val syncStatus = message.syncStatus

    when {
        isMessageRead -> {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                if (readCount > 1 && ChatTheme.readCountEnabled) {
                    Text(
                        text = readCount.toString(),
                        modifier = Modifier.padding(horizontal = 2.dp),
                        style = ChatTheme.typography.footnote,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_message_seen),
                    contentDescription = null,
                    tint = ChatTheme.colors.primaryAccent,
                )
            }
        }
        syncStatus == SyncStatus.SYNC_NEEDED || syncStatus == SyncStatus.AWAITING_ATTACHMENTS -> {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.stream_compose_ic_clock),
                contentDescription = null,
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
        syncStatus == SyncStatus.COMPLETED -> {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.stream_compose_message_sent),
                contentDescription = null,
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

@Preview(showBackground = true, name = "MessageReadStatusIcon Preview (Seen message)")
@Composable
private fun SeenMessageReadStatusIcon() {
    ChatTheme {
        MessageReadStatusIcon(
            message = PreviewMessageData.message2,
            isMessageRead = true,
            readCount = 3,
        )
    }
}