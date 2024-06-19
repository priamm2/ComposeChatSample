package com.example.composechatsample.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.Result
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

public class MediaGalleryPreviewViewModel(
    private val chatClient: ChatClient,
    private val clientState: ClientState,
    private val messageId: String,
    private val skipEnrichUrl: Boolean = false,
) : ViewModel() {

    public val user: StateFlow<User?> = chatClient.clientState.user

    internal var hasCompleteMessage: Boolean = false

    public var message: Message by mutableStateOf(Message())
        internal set

    public var isSharingInProgress: Boolean by mutableStateOf(false)

    public var promptedAttachment: Attachment? by mutableStateOf(null)

    public var connectionState: ConnectionState by mutableStateOf(ConnectionState.Offline)
        private set

    public var isShowingOptions: Boolean by mutableStateOf(false)
        private set

    public var isShowingGallery: Boolean by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            fetchMessage()
            observeConnectionStateChanges()
        }
    }

    private suspend fun fetchMessage() {
        val result = chatClient.getMessage(messageId).await()

        if (result is Result.Success) {
            this.message = result.value
            hasCompleteMessage = true
        }
    }

    private suspend fun observeConnectionStateChanges() {
        clientState.connectionState.collect { connectionState ->
            when (connectionState) {
                is ConnectionState.Connected -> {
                    onConnected()
                    this.connectionState = connectionState
                }
                is ConnectionState.Connecting -> this.connectionState = connectionState
                is ConnectionState.Offline -> this.connectionState = connectionState
            }
        }
    }

    private suspend fun onConnected() {
        if (message.id.isEmpty() || !hasCompleteMessage) {
            fetchMessage()
        }
    }

    public fun toggleMediaOptions(isShowingOptions: Boolean) {
        this.isShowingOptions = isShowingOptions
    }

    public fun toggleGallery(isShowingGallery: Boolean) {
        this.isShowingGallery = isShowingGallery
    }

    public fun deleteCurrentMediaAttachment(
        currentMediaAttachment: Attachment,
        skipEnrichUrl: Boolean = this.skipEnrichUrl,
    ) {
        val attachments = message.attachments
        val numberOfAttachments = attachments.size

        if (message.text.isNotEmpty() || numberOfAttachments > 1) {
            message = message.copy(
                attachments = attachments.filterNot {
                    it.url == currentMediaAttachment.url
                },
                skipEnrichUrl = skipEnrichUrl,
            )
            chatClient.updateMessage(message = message).enqueue()
        } else if (message.text.isEmpty() && numberOfAttachments == 1) {
            chatClient.deleteMessage(message.id).enqueue { result ->
                if (result is Result.Success) {
                    message = result.value
                }
            }
        }
    }
}