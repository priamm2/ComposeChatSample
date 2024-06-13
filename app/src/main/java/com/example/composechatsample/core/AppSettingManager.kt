package com.example.composechatsample.core

import com.example.composechatsample.core.models.App
import com.example.composechatsample.core.models.AppSettings
import com.example.composechatsample.core.models.FileUploadConfig
import com.example.composechatsample.log.StreamLog

internal class AppSettingManager(private val chatApi: ChatApi) {

    private var appSettings: AppSettings? = null

    fun loadAppSettings() {
        if (appSettings == null) {
            chatApi.appSettings().enqueue { result ->
                if (result is Result.Success) {
                    this.appSettings = result.value
                } else if (result is Result.Failure) {
                    when (val cause = result.value.extractCause()) {
                        null -> StreamLog.e(TAG) { "[loadAppSettings] failed: ${result.value}" }
                        else -> StreamLog.e(TAG, cause) { "[loadAppSettings] failed: ${result.value}" }
                    }
                }
            }
        }
    }

    fun getAppSettings(): AppSettings = appSettings ?: createDefaultAppSettings()

    fun clear() {
        appSettings = null
    }

    companion object {
        private const val TAG = "Chat:AppSettingManager"

        fun createDefaultAppSettings(): AppSettings {
            return AppSettings(
                app = App(
                    name = "",
                    fileUploadConfig = FileUploadConfig(
                        allowedFileExtensions = emptyList(),
                        allowedMimeTypes = emptyList(),
                        blockedFileExtensions = emptyList(),
                        blockedMimeTypes = emptyList(),
                        sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                    ),
                    imageUploadConfig = FileUploadConfig(
                        allowedFileExtensions = emptyList(),
                        allowedMimeTypes = emptyList(),
                        blockedFileExtensions = emptyList(),
                        blockedMimeTypes = emptyList(),
                        sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                    ),
                ),
            )
        }
    }
}