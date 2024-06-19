package com.example.composechatsample.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.common.DateFormatType
import com.example.composechatsample.screen.MessageItemState
import com.example.composechatsample.screen.components.MessageReadStatusIcon
import com.example.composechatsample.screen.components.Timestamp
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
@Suppress("LongMethod")
public fun MessageFooter(
    messageItem: MessageItemState,
) {
    val message = messageItem.message
    val hasThread = message.threadParticipants.isNotEmpty()
    val alignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)

    if (hasThread && !messageItem.isInThread) {
        val replyCount = message.replyCount
        MessageThreadFooter(
            participants = message.threadParticipants,
            messageAlignment = alignment,
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_footnote,
                replyCount,
                replyCount,
            ),
        )
    }

    Column(horizontalAlignment = alignment.contentAlignment) {
        MessageTranslatedLabel(messageItem)
        if (messageItem.showMessageFooter) {
            var showEditLabel by remember { mutableStateOf(message.messageTextUpdatedAt != null) }
            var showEditInfo by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!messageItem.isMine) {
                    Text(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f, fill = false),
                        text = message.user.name,
                        style = ChatTheme.typography.footnote,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                } else {
                    MessageReadStatusIcon(
                        modifier = Modifier.padding(end = 4.dp),
                        message = messageItem.message,
                        isMessageRead = messageItem.isMessageRead,
                        readCount = messageItem.messageReadBy.size,
                    )
                }

                val updatedAt = message.updatedAt
                val createdAt = message.createdAt ?: message.createdLocallyAt
                val date = when {
                    createdAt == null -> updatedAt
                    updatedAt == null -> createdAt
                    else -> when (updatedAt.after(createdAt)) {
                        true -> updatedAt
                        else -> createdAt
                    }
                }
                if (date != null) {
                    Timestamp(date = date, formatType = DateFormatType.TIME)
                }
                if (showEditLabel) {
                    Text(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                        text = "·",
                        style = ChatTheme.typography.footnote,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                    Text(
                        modifier = Modifier
                            .clickable {
                                showEditLabel = !showEditLabel
                                showEditInfo = !showEditInfo
                            },
                        text = LocalContext.current.getString(R.string.stream_compose_message_list_footnote_edited),
                        style = ChatTheme.typography.footnote,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                }
            }
            if (showEditInfo) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clickable {
                            showEditLabel = !showEditLabel
                            showEditInfo = !showEditInfo
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f, fill = false),
                        text = LocalContext.current.getString(R.string.stream_compose_message_list_footnote_edited),
                        style = ChatTheme.typography.footnote,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                    Timestamp(date = message.messageTextUpdatedAt, formatType = DateFormatType.RELATIVE)
                }
            }
        }
    }
}