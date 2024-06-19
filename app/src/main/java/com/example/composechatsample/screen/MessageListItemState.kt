package com.example.composechatsample.screen

import com.example.composechatsample.DeletedMessageVisibility
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.messages.MessageFocusState
import com.example.composechatsample.screen.messages.MessagePosition
import java.util.Date

public sealed class MessageListItemState

public sealed class HasMessageListItemState : MessageListItemState() {

    public abstract val message: Message
}

public data class MessageItemState(
    public override val message: Message = Message(),
    public val parentMessageId: String? = null,
    public val isMine: Boolean = false,
    public val isInThread: Boolean = false,
    public val showMessageFooter: Boolean = false,
    public val currentUser: User? = null,
    public val groupPosition: List<MessagePosition> = listOf(MessagePosition.NONE),
    public val isMessageRead: Boolean = false,
    public val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN,
    public val focusState: MessageFocusState? = null,
    public val messageReadBy: List<ChannelUserRead> = emptyList(),
) : HasMessageListItemState()

public data class DateSeparatorItemState(
    val date: Date,
) : MessageListItemState()

public data class ThreadDateSeparatorItemState(
    public val date: Date,
    public val replyCount: Int,
) : MessageListItemState()

public data class SystemMessageItemState(
    public override val message: Message,
) : HasMessageListItemState()

public data class TypingItemState(
    public val typingUsers: List<User>,
) : MessageListItemState()

public data object EmptyThreadPlaceholderItemState : MessageListItemState()

public data class UnreadSeparatorItemState(
    val unreadCount: Int,
) : MessageListItemState()

public data class StartOfTheChannelItemState(
    val channel: Channel,
) : MessageListItemState()