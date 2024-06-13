package com.example.composechatsample.core.errors

public data class ChatError(
    val code: Int = -1,
    var message: String = "",
    var statusCode: Int = -1,
    val exceptionFields: Map<String, String> = mapOf(),
    var moreInfo: String = "",
    val details: List<ChatErrorDetail> = emptyList(),
    var duration: String = "",
)


public data class ChatErrorDetail(
    public val code: Int,
    public val messages: List<String>,
)