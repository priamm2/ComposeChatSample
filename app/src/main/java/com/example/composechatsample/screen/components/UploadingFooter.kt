package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.messages.OwnedMessageVisibilityContent
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun UploadingFooter(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val uploadedCount = message.attachments.count { it.uploadState is Attachment.UploadState.Success }
    val totalCount = message.attachments.size

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        OwnedMessageVisibilityContent(message = message)

        Text(
            text = stringResource(
                id = R.string.stream_compose_upload_file_count,
                uploadedCount + 1,
                totalCount,
            ),
            style = ChatTheme.typography.body,
            textAlign = TextAlign.End,
        )
    }
}