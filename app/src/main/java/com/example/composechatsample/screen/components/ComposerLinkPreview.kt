package com.example.composechatsample.screen.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.composechatsample.R
import com.example.composechatsample.common.rememberStreamImagePainter
import com.example.composechatsample.core.addSchemeToUrlIfNeeded
import com.example.composechatsample.core.imagePreviewUrl
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.ui.theme.ChatTheme


private const val TAG = "ComposerLinkPreview"

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ComposerLinkPreview(
    modifier: Modifier = Modifier,
    linkPreview: LinkPreview,
    onClick: ((linkPreview: LinkPreview) -> Unit)? = null,
) {
    var previewClosed by rememberSaveable { mutableStateOf(false) }

    if (previewClosed) {
        return
    }

    val context = LocalContext.current
    val attachment = linkPreview.attachment
    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val errorMessage = stringResource(id = R.string.stream_compose_message_list_error_cannot_open_link, previewUrl)

    Row(
        modifier = modifier
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    try {
                        onClick?.invoke(linkPreview) ?: onLinkPreviewClick(context, linkPreview)
                    } catch (e: ActivityNotFoundException) {
                        StreamLog.e(TAG, e) { "[onLinkPreviewClick] failed: $e" }
                        Toast
                            .makeText(context, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val theme = ChatTheme.messageComposerTheme.linkPreview
        ComposerLinkImagePreview(attachment)
        ComposerVerticalSeparator()
        Column(
            modifier = Modifier.weight(1f),
        ) {
            ComposerLinkTitle(attachment.title)
            Spacer(modifier = Modifier.height(theme.titleToSubtitle))
            ComposerLinkDescription(attachment.text)
        }
        ComposerLinkCancelIcon { previewClosed = true }
    }
}

@Composable
private fun ComposerLinkImagePreview(attachment: Attachment) {
    val imagePreviewUrl = attachment.imagePreviewUrl ?: return
    val theme = ChatTheme.messageComposerTheme.linkPreview
    val painter = rememberStreamImagePainter(data = imagePreviewUrl)
    Box(
        modifier = Modifier.padding(theme.imagePadding),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .height(theme.imageSize.height)
                .width(theme.imageSize.width)
                .clip(theme.imageShape),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ComposerVerticalSeparator() {
    val theme = ChatTheme.messageComposerTheme.linkPreview
    Box(
        modifier = Modifier.padding(
            start = theme.separatorMarginStart,
            end = theme.separatorMarginEnd,
        ),
    ) {
        Box(
            modifier = Modifier
                .height(theme.separatorSize.height)
                .width(theme.separatorSize.width)
                .background(ChatTheme.colors.primaryAccent),
        )
    }
}

@Composable
private fun ComposerLinkTitle(title: String?) {
    title ?: return
    val textStyle = ChatTheme.messageComposerTheme.linkPreview.title
    Text(
        text = title,
        style = textStyle.style,
        color = textStyle.color,
        maxLines = textStyle.maxLines,
        overflow = textStyle.overflow,
    )
}

@Composable
private fun ComposerLinkDescription(description: String?) {
    description ?: return
    val textStyle = ChatTheme.messageComposerTheme.linkPreview.subtitle
    Text(
        text = description,
        style = textStyle.style,
        color = textStyle.color,
        maxLines = textStyle.maxLines,
        overflow = textStyle.overflow,
    )
}

@Composable
private fun ComposerLinkCancelIcon(
    onClick: () -> Unit,
) {
    val theme = ChatTheme.messageComposerTheme.linkPreview
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier
                .background(
                    shape = theme.cancelIcon.backgroundShape,
                    color = theme.cancelIcon.backgroundColor,
                ),
            painter = theme.cancelIcon.painter,
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = theme.cancelIcon.tint,
        )
    }
}

private fun onLinkPreviewClick(context: Context, preview: LinkPreview) {
    val previewUrl = preview.attachment.titleLink ?: preview.attachment.ogUrl
    checkNotNull(previewUrl) {
        "Missing preview URL."
    }
    val urlWithScheme = previewUrl.addSchemeToUrlIfNeeded()
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(urlWithScheme),
        ),
    )
}