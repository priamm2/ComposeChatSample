package com.example.composechatsample.viewModel

import androidx.lifecycle.ViewModel
import com.example.composechatsample.core.Call
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.MessageComposerState
import com.example.composechatsample.screen.messages.MessageMode
import com.example.composechatsample.screen.messages.ValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class MessageComposerViewModel(
    private val messageComposerController: MessageComposerController,
) : ViewModel() {

    public val messageComposerState: StateFlow<MessageComposerState> = messageComposerController.state

    public val input: MutableStateFlow<String> = messageComposerController.input

    public val alsoSendToChannel: MutableStateFlow<Boolean> = messageComposerController.alsoSendToChannel

    public val cooldownTimer: MutableStateFlow<Int> = messageComposerController.cooldownTimer

    public val selectedAttachments: MutableStateFlow<List<Attachment>> = messageComposerController.selectedAttachments

    public val validationErrors: MutableStateFlow<List<ValidationError>> = messageComposerController.validationErrors

    public val mentionSuggestions: MutableStateFlow<List<User>> = messageComposerController.mentionSuggestions

    public val commandSuggestions: MutableStateFlow<List<Command>> = messageComposerController.commandSuggestions


    public val linkPreviews: MutableStateFlow<List<LinkPreview>> = messageComposerController.linkPreviews

    public val messageMode: MutableStateFlow<MessageMode> = messageComposerController.messageMode

    public val lastActiveAction: Flow<MessageAction?> = messageComposerController.lastActiveAction

    public val ownCapabilities: StateFlow<Set<String>> = messageComposerController.ownCapabilities

    public fun setMessageInput(value: String): Unit = messageComposerController.setMessageInput(value)

    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean): Unit =
        messageComposerController.setAlsoSendToChannel(alsoSendToChannel)

    public fun setMessageMode(messageMode: MessageMode): Unit = messageComposerController.setMessageMode(messageMode)

    public fun performMessageAction(messageAction: MessageAction): Unit =
        messageComposerController.performMessageAction(messageAction)

    public fun dismissMessageActions(): Unit = messageComposerController.dismissMessageActions()

    public fun addSelectedAttachments(attachments: List<Attachment>): Unit =
        messageComposerController.addSelectedAttachments(attachments)

    public fun removeSelectedAttachment(attachment: Attachment): Unit =
        messageComposerController.removeSelectedAttachment(attachment)

    public fun sendMessage(
        message: Message,
        callback: Call.Callback<Message> = Call.Callback { /* no-op */ },
    ): Unit = messageComposerController.sendMessage(message, callback)

    public fun buildNewMessage(
        message: String,
        attachments: List<Attachment> = emptyList(),
    ): Message = messageComposerController.buildNewMessage(message, attachments)

    public fun leaveThread(): Unit = messageComposerController.leaveThread()

    public fun selectMention(user: User): Unit = messageComposerController.selectMention(user)

    public fun selectCommand(command: Command): Unit = messageComposerController.selectCommand(command)

    public fun toggleCommandsVisibility(): Unit = messageComposerController.toggleCommandsVisibility()

    public fun setTypingUpdatesBuffer(buffer: TypingUpdatesBuffer) {
        messageComposerController.typingUpdatesBuffer = buffer
    }

    public fun clearData(): Unit = messageComposerController.clearData()

    override fun onCleared() {
        super.onCleared()
        messageComposerController.onCleared()
    }
}