package com.example.composechatsample.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.state.ClientState

public class MediaGalleryPreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val clientState: ClientState = chatClient.clientState,
    private val messageId: String,
    private val skipEnrichUrl: Boolean = false,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MediaGalleryPreviewViewModel(
            chatClient = chatClient,
            clientState = clientState,
            messageId = messageId,
            skipEnrichUrl = skipEnrichUrl,
        ) as T
    }
}