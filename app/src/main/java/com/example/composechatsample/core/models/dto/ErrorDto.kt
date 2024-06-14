package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorDto(
    val code: Int = -1,
    val message: String = "",
    val StatusCode: Int = -1,
    val duration: String = "",
    val exception_fields: Map<String, String> = mapOf(),
    val more_info: String = "",
    val details: List<ErrorDetailDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ErrorDetailDto(
    val code: Int = -1,
    val messages: List<String> = emptyList(),
)