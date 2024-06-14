package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
data class AppSettings(
    val app: App,
) {
    companion object {
        const val DEFAULT_SIZE_LIMIT_IN_BYTES: Long = 100 * 1024 * 1024
    }
}

@Immutable
data class App(
    val name: String,
    val fileUploadConfig: FileUploadConfig,
    val imageUploadConfig: FileUploadConfig,
)

@Immutable
public data class FileUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
    val sizeLimitInBytes: Long,
)
