package com.example.composechatsample.common

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.screen.messages.AttachmentsPickerMode
import com.example.composechatsample.screen.messages.Images
import com.example.composechatsample.ui.theme.AttachmentsPickerTabFactory
import com.example.composechatsample.ui.theme.ChatTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

public class AttachmentsPickerImagesTabFactory : AttachmentsPickerTabFactory {

    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = Images

    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_image_picker),
            contentDescription = stringResource(id = R.string.stream_compose_images_option),
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun PickerTabContent(
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        var storagePermissionRequested by rememberSaveable { mutableStateOf(false) }
        val storagePermissionState =
            rememberMultiplePermissionsState(
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                    )
                } else {
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                },
            ) {
                storagePermissionRequested = true
            }

        val context = LocalContext.current
        val storageHelper: StorageHelperWrapper =
            remember { StorageHelperWrapper(context, StorageHelper(), AttachmentFilter()) }

        when (storagePermissionState.allPermissionsGranted) {
            true -> {
                ImagesPicker(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 2.dp,
                        end = 2.dp,
                        bottom = 2.dp,
                    ),
                    images = attachments,
                    onImageSelected = onAttachmentItemSelected,
                )
            }
            else -> {
                val revokedPermissionState = storagePermissionState.revokedPermissions.first()
                MissingPermissionContent(revokedPermissionState)
            }
        }

        val hasPermission = storagePermissionState.allPermissionsGranted

        LaunchedEffect(storagePermissionState.allPermissionsGranted) {
            if (storagePermissionState.allPermissionsGranted) {
                onAttachmentsChanged(
                    storageHelper.getMedia().map { AttachmentPickerItemState(it, false) },
                )
            }
        }

        LaunchedEffect(Unit) {
            if (!hasPermission && !storagePermissionRequested) {
                storagePermissionState.launchMultiplePermissionRequest()
            }
        }
    }
}