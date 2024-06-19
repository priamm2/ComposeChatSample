package com.example.composechatsample.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.composechatsample.R
import com.example.composechatsample.core.hasLink
import com.example.composechatsample.core.isGiphy
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.toSet
import com.example.composechatsample.screen.PreviewMessageData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageOptions(
    options: List<MessageOptionItemState>,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable ColumnScope.(MessageOptionItemState) -> Unit = { option ->
        DefaultMessageOptionItem(
            option = option,
            onMessageOptionSelected = onMessageOptionSelected,
        )
    },
) {
    Column(modifier = modifier) {
        options.forEach { option ->
            key(option.action) {
                itemContent(option)
            }
        }
    }
}

@Composable
internal fun DefaultMessageOptionItem(
    option: MessageOptionItemState,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
) {
    MessageOptionItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChatTheme.dimens.messageOptionsItemHeight)
            .clickable(
                onClick = { onMessageOptionSelected(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
            ),
        option = option,
    )
}

@Composable
public fun defaultMessageOptionsState(
    selectedMessage: Message,
    currentUser: User?,
    isInThread: Boolean,
    ownCapabilities: Set<String>,
): List<MessageOptionItemState> {
    if (selectedMessage.id.isEmpty()) {
        return emptyList()
    }

    val selectedMessageUserId = selectedMessage.user.id

    val isTextOnlyMessage = selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()
    val hasLinks = selectedMessage.attachments.any { it.hasLink() && !it.isGiphy() }
    val isOwnMessage = selectedMessageUserId == currentUser?.id
    val isMessageSynced = selectedMessage.syncStatus == SyncStatus.COMPLETED
    val isMessageFailed = selectedMessage.syncStatus == SyncStatus.FAILED_PERMANENTLY

    // user capabilities
    val canQuoteMessage = ownCapabilities.contains(ChannelCapabilities.QUOTE_MESSAGE)
    val canThreadReply = ownCapabilities.contains(ChannelCapabilities.SEND_REPLY)
    val canPinMessage = ownCapabilities.contains(ChannelCapabilities.PIN_MESSAGE)
    val canDeleteOwnMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_OWN_MESSAGE)
    val canDeleteAnyMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_ANY_MESSAGE)
    val canEditOwnMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_OWN_MESSAGE)
    val canEditAnyMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_ANY_MESSAGE)
    val canMarkAsUnread = ownCapabilities.contains(ChannelCapabilities.READ_EVENTS)
    val canFlagMessage = ownCapabilities.contains(ChannelCapabilities.FLAG_MESSAGE)

    return listOfNotNull(
        if (isOwnMessage && isMessageFailed) {
            MessageOptionItemState(
                title = R.string.stream_compose_resend_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_resend),
                action = Resend(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (isMessageSynced && canQuoteMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_reply),
                action = Reply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (!isInThread && isMessageSynced && canThreadReply) {
            MessageOptionItemState(
                title = R.string.stream_compose_thread_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_thread),
                action = ThreadReply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (canMarkAsUnread) {
            MessageOptionItemState(
                title = R.string.stream_compose_mark_as_unread,
                iconPainter = painterResource(R.drawable.stream_compose_ic_mark_as_unread),
                action = MarkAsUnread(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (isTextOnlyMessage || hasLinks) {
            MessageOptionItemState(
                title = R.string.stream_compose_copy_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_copy),
                action = Copy(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (((isOwnMessage && canEditOwnMessage) || canEditAnyMessage) && !selectedMessage.isGiphy()) {
            MessageOptionItemState(
                title = R.string.stream_compose_edit_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_edit),
                action = Edit(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (canFlagMessage && !isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_flag_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_flag),
                action = Flag(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (isMessageSynced && canPinMessage) {
            MessageOptionItemState(
                title = if (selectedMessage.pinned) R.string.stream_compose_unpin_message else R.string.stream_compose_pin_message,
                action = Pin(selectedMessage),
                iconPainter = painterResource(id = if (selectedMessage.pinned) R.drawable.stream_compose_ic_unpin_message else R.drawable.stream_compose_ic_pin_message),
                iconColor = ChatTheme.colors.textLowEmphasis,
                titleColor = ChatTheme.colors.textHighEmphasis,
            )
        } else {
            null
        },
        if (canDeleteAnyMessage || (isOwnMessage && canDeleteOwnMessage)) {
            MessageOptionItemState(
                title = R.string.stream_compose_delete_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_delete),
                action = Delete(selectedMessage),
                iconColor = ChatTheme.colors.errorAccent,
                titleColor = ChatTheme.colors.errorAccent,
            )
        } else {
            null
        },
    )
}

@Preview(showBackground = true, name = "MessageOptions Preview (Own Message)")
@Composable
private fun MessageOptionsForOwnMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user1,
        syncStatus = SyncStatus.COMPLETED,
    )
}

@Preview(showBackground = true, name = "MessageOptions Preview (Theirs Message)")
@Composable
private fun MessageOptionsForTheirsMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user2,
        syncStatus = SyncStatus.COMPLETED,
    )
}

@Preview(showBackground = true, name = "MessageOptions Preview (Failed Message)")
@Composable
private fun MessageOptionsForFailedMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user1,
        syncStatus = SyncStatus.FAILED_PERMANENTLY,
    )
}

@Composable
private fun MessageOptionsPreview(
    messageUser: User,
    currentUser: User,
    syncStatus: SyncStatus,
) {
    ChatTheme {
        val selectedMMessage = PreviewMessageData.message1.copy(
            user = messageUser,
            syncStatus = syncStatus,
        )

        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = selectedMMessage,
            currentUser = currentUser,
            isInThread = false,
            ownCapabilities = ChannelCapabilities.toSet(),
        )

        MessageOptions(options = messageOptionsStateList, onMessageOptionSelected = {})
    }
}