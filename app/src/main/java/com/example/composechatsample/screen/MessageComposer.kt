package com.example.composechatsample.screen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.composechatsample.R
import com.example.composechatsample.StatefulStreamMediaRecorder
import com.example.composechatsample.core.MediaRecorderState
import com.example.composechatsample.core.extractCause
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.Priority
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.streamLog
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.MessageComposerViewModel
import kotlinx.coroutines.launch

@Composable
public fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder? = null,
    onSendMessage: (Message) -> Unit = { viewModel.sendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.setMessageInput(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.dismissMessageActions() },
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onMentionSelected: (User) -> Unit = { viewModel.selectMention(it) },
    onCommandSelected: (Command) -> Unit = { viewModel.selectCommand(it) },
    onAlsoSendToChannelSelected: (Boolean) -> Unit = { viewModel.setAlsoSendToChannel(it) },
    onRecordingSaved: (Attachment) -> Unit = { viewModel.addSelectedAttachments(listOf(it)) },
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerHeaderContent(
            messageComposerState = it,
            onCancelAction = onCancelAction,
            onLinkPreviewClick = onLinkPreviewClick,
        )
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerFooterContent(
            messageComposerState = it,
            onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        )
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerIntegrations(
            messageInputState = it,
            onAttachmentsClick = onAttachmentsClick,
            onCommandsClick = onCommandsClick,
            ownCapabilities = it.ownCapabilities,
        )
    },
    label: @Composable (MessageComposerState) -> Unit = { DefaultComposerLabel(it.ownCapabilities) },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerInputContent(
            messageComposerState = it,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            label = label,
        )
    },
    audioRecordingContent: @Composable RowScope.(StatefulStreamMediaRecorder) -> Unit = {
        DefaultMessageComposerAudioRecordingContent(it)
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        DefaultMessageComposerTrailingContent(
            value = it.inputValue,
            coolDownTime = it.coolDownTime,
            validationErrors = it.validationErrors,
            attachments = it.attachments,
            ownCapabilities = it.ownCapabilities,
            isInEditMode = it.action is Edit,
            onSendMessage = { input, attachments ->
                val message = viewModel.buildNewMessage(input, attachments)

                onSendMessage(message)
            },
            onRecordingSaved = onRecordingSaved,
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
        )
    },
) {
    val messageComposerState by viewModel.messageComposerState.collectAsState()

    MessageComposer(
        modifier = modifier,
        onSendMessage = { text, attachments ->
            val messageWithData = viewModel.buildNewMessage(text, attachments)

            onSendMessage(messageWithData)
        },
        onMentionSelected = onMentionSelected,
        onCommandSelected = onCommandSelected,
        onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        headerContent = headerContent,
        footerContent = footerContent,
        mentionPopupContent = mentionPopupContent,
        commandPopupContent = commandPopupContent,
        integrations = integrations,
        input = input,
        audioRecordingContent = audioRecordingContent,
        trailingContent = trailingContent,
        messageComposerState = messageComposerState,
        onCancelAction = onCancelAction,
        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
    )
}

@Composable
public fun MessageComposer(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    modifier: Modifier = Modifier,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder? = null,
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = {},
    onAttachmentRemoved: (Attachment) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onMentionSelected: (User) -> Unit = {},
    onCommandSelected: (Command) -> Unit = {},
    onAlsoSendToChannelSelected: (Boolean) -> Unit = {},
    onRecordingSaved: (Attachment) -> Unit = {},
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerHeaderContent(
            messageComposerState = it,
            onCancelAction = onCancelAction,
            onLinkPreviewClick = onLinkPreviewClick,
        )
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerFooterContent(
            messageComposerState = it,
            onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        )
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerIntegrations(
            messageInputState = it,
            onAttachmentsClick = onAttachmentsClick,
            onCommandsClick = onCommandsClick,
            ownCapabilities = messageComposerState.ownCapabilities,
        )
    },
    label: @Composable (MessageComposerState) -> Unit = { DefaultComposerLabel(messageComposerState.ownCapabilities) },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerInputContent(
            messageComposerState = messageComposerState,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            label = label,
        )
    },
    audioRecordingContent: @Composable RowScope.(StatefulStreamMediaRecorder) -> Unit = {
        DefaultMessageComposerAudioRecordingContent(it)
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        DefaultMessageComposerTrailingContent(
            value = it.inputValue,
            coolDownTime = it.coolDownTime,
            validationErrors = it.validationErrors,
            attachments = it.attachments,
            onSendMessage = onSendMessage,
            ownCapabilities = messageComposerState.ownCapabilities,
            isInEditMode = it.action is Edit,
            onRecordingSaved = onRecordingSaved,
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
        )
    },
) {
    val (_, _, activeAction, validationErrors, mentionSuggestions, commandSuggestions) = messageComposerState
    val snackbarHostState = remember { SnackbarHostState() }

    val isRecording = statefulStreamMediaRecorder?.mediaRecorderState?.value

    MessageInputValidationError(
        validationErrors = validationErrors,
        snackbarHostState = snackbarHostState,
    )

    Surface(
        modifier = modifier,
        elevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            headerContent(messageComposerState)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Bottom,
            ) {
                if (activeAction !is Edit) {
                    integrations(messageComposerState)
                } else {
                    Spacer(
                        modifier = Modifier.size(16.dp),
                    )
                }

                if (isRecording == MediaRecorderState.RECORDING) {
                    audioRecordingContent(statefulStreamMediaRecorder)
                } else {
                    input(messageComposerState)
                }

                trailingContent(messageComposerState)
            }

            footerContent(messageComposerState)
        }

        if (snackbarHostState.currentSnackbarData != null) {
            SnackbarPopup(snackbarHostState = snackbarHostState)
        }

        if (mentionSuggestions.isNotEmpty()) {
            mentionPopupContent(mentionSuggestions)
        }

        if (commandSuggestions.isNotEmpty()) {
            commandPopupContent(commandSuggestions)
        }
    }
}

@Composable
public fun DefaultMessageComposerHeaderContent(
    messageComposerState: MessageComposerState,
    onCancelAction: () -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
) {
    val activeAction = messageComposerState.action

    if (activeAction != null) {
        MessageInputOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
            activeAction = activeAction,
            onCancelAction = onCancelAction,
        )
    }
    if (ChatTheme.isComposerLinkPreviewEnabled && messageComposerState.linkPreviews.isNotEmpty()) {
        ComposerLinkPreview(
            linkPreview = messageComposerState.linkPreviews.first(),
            onClick = onLinkPreviewClick,
        )
    }
}

@Composable
public fun DefaultMessageComposerFooterContent(
    messageComposerState: MessageComposerState,
    onAlsoSendToChannelSelected: (Boolean) -> Unit,
) {
    if (messageComposerState.messageMode is MessageMode.MessageThread) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = messageComposerState.alsoSendToChannel,
                onCheckedChange = { onAlsoSendToChannelSelected(it) },
                colors = CheckboxDefaults.colors(ChatTheme.colors.primaryAccent),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.stream_compose_message_composer_show_in_channel),
                color = ChatTheme.colors.textLowEmphasis,
                textAlign = TextAlign.Center,
                style = ChatTheme.typography.body,
            )
        }
    }
}

@Composable
internal fun DefaultMentionPopupContent(
    mentionSuggestions: List<User>,
    onMentionSelected: (User) -> Unit,
) {
    MentionSuggestionList(
        users = mentionSuggestions,
        onMentionSelected = { onMentionSelected(it) },
    )
}

@Composable
internal fun DefaultCommandPopupContent(
    commandSuggestions: List<Command>,
    onCommandSelected: (Command) -> Unit,
) {
    CommandSuggestionList(
        commands = commandSuggestions,
        onCommandSelected = { onCommandSelected(it) },
    )
}

@Composable
internal fun DefaultComposerIntegrations(
    messageInputState: MessageComposerState,
    onAttachmentsClick: () -> Unit,
    onCommandsClick: () -> Unit,
    ownCapabilities: Set<String>,
) {
    val hasTextInput = messageInputState.inputValue.isNotEmpty()
    val hasAttachments = messageInputState.attachments.isNotEmpty()
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
    val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

    val canSendMessage = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val canSendAttachments = ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)

    if (canSendMessage) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canSendAttachments) {
                IconButton(
                    enabled = isAttachmentsButtonEnabled,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_attachments),
                            contentDescription = stringResource(id = R.string.stream_compose_attachments),
                            tint = if (isAttachmentsButtonEnabled) {
                                ChatTheme.colors.textLowEmphasis
                            } else {
                                ChatTheme.colors.disabled
                            },
                        )
                    },
                    onClick = onAttachmentsClick,
                )
            }

            val commandsButtonTint = if (hasCommandSuggestions && isCommandsButtonEnabled) {
                ChatTheme.colors.primaryAccent
            } else if (isCommandsButtonEnabled) {
                ChatTheme.colors.textLowEmphasis
            } else {
                ChatTheme.colors.disabled
            }

            AnimatedVisibility(visible = messageInputState.hasCommands) {
                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    enabled = isCommandsButtonEnabled,
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_command),
                            contentDescription = null,
                            tint = commandsButtonTint,
                        )
                    },
                    onClick = onCommandsClick,
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
internal fun DefaultComposerLabel(ownCapabilities: Set<String>) {
    val text =
        if (ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)) {
            stringResource(id = R.string.stream_compose_message_label)
        } else {
            stringResource(id = R.string.stream_compose_cannot_send_messages_label)
        }

    Text(
        text = text,
        color = ChatTheme.colors.textLowEmphasis,
    )
}


@Composable
private fun RowScope.DefaultComposerInputContent(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    label: @Composable (MessageComposerState) -> Unit,
) {
    MessageInput(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .weight(1f),
        label = label,
        messageComposerState = messageComposerState,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
    )
}

@Composable
internal fun RowScope.DefaultMessageComposerAudioRecordingContent(
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .weight(1f),
    ) {
        val amplitudeSample = statefulStreamMediaRecorder.latestMaxAmplitude.value
        val recordingDuration = statefulStreamMediaRecorder.activeRecordingDuration.value

        val recordingDurationFormatted by remember(recordingDuration) {
            derivedStateOf {
                val remainder = recordingDuration % 60_000
                val seconds = String.format("%02d", remainder / 1000)
                val minutes = String.format("%02d", (recordingDuration - remainder) / 60_000)

                "$minutes:$seconds"
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.stream_compose_ic_circle),
                tint = Color.Red,
                contentDescription = null,
            )

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = recordingDurationFormatted,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        RunningWaveForm(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
                .height(20.dp),
            maxInputValue = 20_000,
            barWidth = 8.dp,
            barGap = 2.dp,
            restartKey = true,
            newValueKey = amplitudeSample.key,
            latestValue = amplitudeSample.value,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun DefaultMessageComposerTrailingContent(
    value: String,
    coolDownTime: Int,
    attachments: List<Attachment>,
    validationErrors: List<ValidationError>,
    ownCapabilities: Set<String>,
    isInEditMode: Boolean,
    onSendMessage: (String, List<Attachment>) -> Unit,
    onRecordingSaved: (Attachment) -> Unit,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder?,
) {
    val isSendButtonEnabled = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val isInputValid by lazy { (value.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty() }
    val sendButtonDescription = stringResource(id = R.string.stream_compose_cd_send_button)
    val recordAudioButtonDescription = stringResource(id = R.string.stream_compose_cd_record_audio_message)
    var permissionsRequested by rememberSaveable { mutableStateOf(false) }

    val isRecording = statefulStreamMediaRecorder?.mediaRecorderState?.value

    // TODO test permissions on lower APIs etc
    val storageAndRecordingPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.RECORD_AUDIO,
            )
        } else {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        },
    ) {
        permissionsRequested = true
    }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    if (coolDownTime > 0 && !isInEditMode) {
        CoolDownIndicator(coolDownTime = coolDownTime)
    } else {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (statefulStreamMediaRecorder != null) {
                Box(
                    modifier = Modifier
                        .semantics { contentDescription = recordAudioButtonDescription }
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {

                                    fun handleAudioRecording() = coroutineScope.launch {
                                        awaitPointerEventScope {
                                            statefulStreamMediaRecorder.startAudioRecording(
                                                context = context,
                                                recordingName = "audio_recording_${Date()}",
                                            )

                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Main)

                                                if (event.changes.all { it.changedToUp() }) {
                                                    statefulStreamMediaRecorder
                                                        .stopRecording()
                                                        .onSuccess {
                                                            StreamLog.i("MessageComposer") {
                                                                "[onRecordingSaved] attachment: $it"
                                                            }
                                                            onRecordingSaved(it.attachment)
                                                        }
                                                        .onError {
                                                            streamLog(throwable = it.extractCause()) {
                                                                "Could not save audio recording: ${it.message}"
                                                            }
                                                        }
                                                    break
                                                }
                                            }
                                        }
                                    }

                                    when {
                                        !storageAndRecordingPermissionState.allPermissionsGranted -> {
                                            storageAndRecordingPermissionState.launchMultiplePermissionRequest()
                                        }

                                        isRecording == MediaRecorderState.UNINITIALIZED -> {
                                            handleAudioRecording()
                                        }

                                        else -> streamLog(Priority.ERROR) { "Could not start audio recording" }
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val layoutDirection = LocalLayoutDirection.current

                    Icon(
                        modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic_active),
                        contentDescription = stringResource(id = R.string.stream_compose_record_audio_message),
                        tint = if (isRecording == MediaRecorderState.RECORDING) {
                            ChatTheme.colors.primaryAccent
                        } else {
                            ChatTheme.colors.textLowEmphasis
                        },
                    )
                }
            }
        }

        IconButton(
            modifier = Modifier
                .semantics { contentDescription = sendButtonDescription },
            enabled = isSendButtonEnabled && isInputValid,
            content = {
                val layoutDirection = LocalLayoutDirection.current

                Icon(
                    modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
                    painter = painterResource(id = R.drawable.stream_compose_ic_send),
                    contentDescription = stringResource(id = R.string.stream_compose_send_message),
                    tint = if (isInputValid) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
                )
            },
            onClick = {
                if (isInputValid) {
                    onSendMessage(value, attachments)
                }
            },
        )
    }

}

@Composable
private fun MessageInputValidationError(validationErrors: List<ValidationError>, snackbarHostState: SnackbarHostState) {
    if (validationErrors.isNotEmpty()) {
        val firstValidationError = validationErrors.first()

        val errorMessage = when (firstValidationError) {
            is ValidationError.MessageLengthExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_message_length,
                    firstValidationError.maxMessageLength,
                )
            }
            is ValidationError.AttachmentCountExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_attachment_count,
                    firstValidationError.maxAttachmentCount,
                )
            }
            is ValidationError.AttachmentSizeExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_file_size,
                    MediaStringUtil.convertFileSizeByteCount(firstValidationError.maxAttachmentSize),
                )
            }
            is ValidationError.ContainsLinksWhenNotAllowed -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_sending_links_not_allowed,
                )
            }
        }

        val context = LocalContext.current
        LaunchedEffect(validationErrors.size) {
            if (firstValidationError is ValidationError.ContainsLinksWhenNotAllowed ||
                firstValidationError is ValidationError.AttachmentSizeExceeded
            ) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = context.getString(R.string.stream_compose_ok),
                    duration = SnackbarDuration.Indefinite,
                )
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun SnackbarPopup(snackbarHostState: SnackbarHostState) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider()) {
        SnackbarHost(hostState = snackbarHostState)
    }
}