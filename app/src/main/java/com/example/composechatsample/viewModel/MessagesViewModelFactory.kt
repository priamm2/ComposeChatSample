package com.example.composechatsample.viewModel

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.composechatsample.common.DefaultStreamMediaRecorder
import com.example.composechatsample.common.StreamMediaRecorder
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.state.ClientState
import java.io.File

public class MessagesViewModelFactory(
    private val context: Context,
    private val channelId: String,
    private val messageId: String? = null,
    private val parentMessageId: String? = null,
    private val autoTranslationEnabled: Boolean = false,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val mediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(context.applicationContext),
    private val userLookupHandler: UserLookupHandler = DefaultUserLookupHandler(chatClient, channelId),
    private val fileToUriConverter: (File) -> String = { file -> file.toUri().toString() },
    private val messageLimit: Int = MessageListController.DEFAULT_MESSAGES_LIMIT,
    private val clipboardHandler: ClipboardHandler = ClipboardHandlerImpl(
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
        autoTranslationEnabled = autoTranslationEnabled,
        getCurrentUser = { chatClient.getCurrentUser() },
    ),
    private val enforceUniqueReactions: Boolean = true,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val showSystemMessages: Boolean = true,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    private val threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
    private val showDateSeparatorInEmptyThread: Boolean = false,
    private val showThreadSeparatorInEmptyThread: Boolean = false,
    private val threadLoadOlderToNewer: Boolean = false,
    private val isComposerLinkPreviewEnabled: Boolean = false,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    chatClient = chatClient,
                    mediaRecorder = mediaRecorder,
                    userLookupHandler = userLookupHandler,
                    fileToUri = fileToUriConverter,
                    channelCid = channelId,
                    messageLimit = messageLimit,
                    maxAttachmentCount = maxAttachmentCount,
                    messageId = messageId,
                    isLinkPreviewEnabled = isComposerLinkPreviewEnabled,
                ),
            )
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                MessageListController(
                    cid = channelId,
                    clipboardHandler = clipboardHandler,
                    threadLoadOrderOlderToNewer = threadLoadOlderToNewer,
                    messageId = messageId,
                    parentMessageId = parentMessageId,
                    messageLimit = messageLimit,
                    chatClient = chatClient,
                    clientState = clientState,
                    enforceUniqueReactions = enforceUniqueReactions,
                    showSystemMessages = showSystemMessages,
                    deletedMessageVisibility = deletedMessageVisibility,
                    messageFooterVisibility = messageFooterVisibility,
                    dateSeparatorHandler = dateSeparatorHandler,
                    threadDateSeparatorHandler = threadDateSeparatorHandler,
                    messagePositionHandler = messagePositionHandler,
                    showDateSeparatorInEmptyThread = showDateSeparatorInEmptyThread,
                    showThreadSeparatorInEmptyThread = showThreadSeparatorInEmptyThread,
                ),
            )
        },
        AttachmentsPickerViewModel::class.java to {
            AttachmentsPickerViewModel(
                StorageHelperWrapper(context, StorageHelper(), AttachmentFilter()),
            )
        },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageListViewModelFactory can only create instances of " +
                    "the following classes: ${factories.keys.joinToString { it.simpleName }}",
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}