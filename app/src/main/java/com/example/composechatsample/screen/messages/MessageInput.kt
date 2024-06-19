package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.screen.components.InputField
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageInput(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = DefaultMessageInputMaxLines,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    label: @Composable (MessageComposerState) -> Unit = {
        DefaultComposerLabel(ownCapabilities = messageComposerState.ownCapabilities)
    },
    innerLeadingContent: @Composable RowScope.() -> Unit = {},
    innerTrailingContent: @Composable RowScope.() -> Unit = {},
) {
    val (value, attachments, activeAction) = messageComposerState
    val canSendMessage = messageComposerState.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)

    InputField(
        modifier = modifier,
        value = value,
        maxLines = maxLines,
        onValueChange = onValueChange,
        enabled = canSendMessage,
        innerPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Column {
                if (activeAction is Reply) {
                    QuotedMessage(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        message = activeAction.message,
                        currentUser = messageComposerState.currentUser,
                        replyMessage = null,
                        onLongItemClick = {},
                        onQuotedMessageClick = {},
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (attachments.isNotEmpty() && activeAction !is Edit) {
                    val previewFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }

                    previewFactory?.previewContent?.invoke(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        attachments,
                        onAttachmentRemoved,
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    innerLeadingContent()

                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()

                        if (value.isEmpty()) {
                            label(messageComposerState)
                        }
                    }

                    innerTrailingContent()
                }
            }
        },
    )
}

private const val DefaultMessageInputMaxLines = 6