package com.example.composechatsample.common

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Attachment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun attachmentDownloadState(): Pair<PermissionState, MutableState<Attachment?>> {
    var writePermissionRequested by rememberSaveable { mutableStateOf(false) }
    val writePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) {
        writePermissionRequested = true
    }

    val downloadPayload = remember { mutableStateOf<Attachment?>(null) }

    val context = LocalContext.current

    LaunchedEffect(writePermissionState.status.isGranted) {
        if (writePermissionState.status.isGranted) {
            downloadPayload.value?.let {
                onDownloadPermissionGranted(context, it)
                downloadPayload.value = null
            }
        }
    }

    return writePermissionState to downloadPayload
}

@OptIn(ExperimentalPermissionsApi::class)
internal fun onDownloadHandleRequest(
    context: Context,
    payload: Attachment,
    permissionState: PermissionState,
    downloadPayload: MutableState<Attachment?>,
) {
    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()) ||
        permissionState.status.isGranted
    ) {
        onDownloadPermissionGranted(context, payload)
        downloadPayload.value = null
    } else {
        downloadPayload.value = payload
        onDownloadPermissionRequired(context, permissionState)
        context.onPermissionRequested(permissionState.permission)
    }
}

internal fun onDownloadPermissionGranted(context: Context, payload: Attachment) {
    payload.let {
        ChatClient
            .instance()
            .downloadAttachment(context, it)
            .enqueue()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
internal fun onDownloadPermissionRequired(context: Context, permissionState: PermissionState) {
    if (!context.wasPermissionRequested(permissionState.permission) || permissionState.status.shouldShowRationale) {
        permissionState.launchPermissionRequest()
    } else {
        context.openSystemSettings()
    }
}