package com.example.composechatsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.painterResource
import com.example.composechatsample.common.DefaultStreamMediaRecorder
import com.example.composechatsample.common.StreamMediaRecorder
import com.example.composechatsample.core.models.ReactionSortingByLastReactionAt
import com.example.composechatsample.screen.MessagesScreen
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.MessageComposerTheme
import com.example.composechatsample.ui.theme.StreamColors
import com.example.composechatsample.ui.theme.StreamTypography
import com.example.composechatsample.viewModel.MessagesViewModelFactory


class MessagesActivity : BaseConnectedActivity() {

    private val streamMediaRecorder: StreamMediaRecorder by lazy { DefaultStreamMediaRecorder(applicationContext) }
    private val statefulStreamMediaRecorder by lazy { StatefulStreamMediaRecorder(streamMediaRecorder) }

    private val factory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
            isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
            deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val colors = if (isSystemInDarkTheme()) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
            val typography = StreamTypography.defaultTypography()
            ChatTheme(
                colors = colors,
                typography = typography,
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
                allowUIAutomationTest = true,
                messageComposerTheme = MessageComposerTheme.defaultTheme(typography).let { messageComposerTheme ->
                    messageComposerTheme.copy(
                        attachmentCancelIcon = messageComposerTheme.attachmentCancelIcon.copy(
                            painter = painterResource(id = R.drawable.stream_compose_ic_clear),
                            tint = colors.overlayDark,
                            backgroundColor = colors.appBackground,
                        ),
                    )
                },
            ) {
                MessagesScreen(
                    viewModelFactory = factory,
                    reactionSorting = ReactionSortingByLastReactionAt,
                    onBackPressed = { finish() },
                    onHeaderTitleClick = {},
                )

                // MyCustomUi()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (statefulStreamMediaRecorder.mediaRecorderState.value == MediaRecorderState.RECORDING) {
            streamMediaRecorder.stopRecording()
        } else {
            streamMediaRecorder.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamMediaRecorder.stopRecording()
    }

    companion object {
        private const val TAG = "MessagesActivity"
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String? = null,
            parentMessageId: String? = null,
        ): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}