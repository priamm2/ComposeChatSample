package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.core.models.User

public data class MessageComposerState @JvmOverloads constructor(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
    val validationErrors: List<ValidationError> = emptyList(),
    val mentionSuggestions: List<User> = emptyList(),
    val commandSuggestions: List<Command> = emptyList(),
    val linkPreviews: List<LinkPreview> = emptyList(),
    val coolDownTime: Int = 0,
    val messageMode: MessageMode = MessageMode.Normal,
    val alsoSendToChannel: Boolean = false,
    val ownCapabilities: Set<String> = setOf(),
    val hasCommands: Boolean = false,
    val currentUser: User? = null,
    val recording: RecordingState = RecordingState.Idle,
)