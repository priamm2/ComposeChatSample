package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppSettingsResponse(
    val app: AppDto,
)

@JsonClass(generateAdapter = true)
data class AppDto(
    val name: String,
    val file_upload_config: FileUploadConfigDto,
    val image_upload_config: FileUploadConfigDto,
)

@JsonClass(generateAdapter = true)
data class FileUploadConfigDto(
    val allowed_file_extensions: List<String>,
    val allowed_mime_types: List<String>,
    val blocked_file_extensions: List<String>,
    val blocked_mime_types: List<String>,
    val size_limit: Long?,
)