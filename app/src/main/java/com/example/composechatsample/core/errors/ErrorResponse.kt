package com.example.composechatsample.core.errors

data class ErrorResponse(
    val code: Int = -1,
    var message: String = "",
    var statusCode: Int = -1,
    val exceptionFields: Map<String, String> = mapOf(),
    var moreInfo: String = "",
    val details: List<ErrorDetail> = emptyList(),
) {
    var duration: String = ""
}