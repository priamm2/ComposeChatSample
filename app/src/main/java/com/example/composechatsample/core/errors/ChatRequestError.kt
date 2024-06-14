package com.example.composechatsample.core.errors

import java.io.IOException

internal class ChatRequestError(
    message: String,
    val streamCode: Int,
    val statusCode: Int,
    cause: Throwable? = null,
) : IOException(message, cause) {
    override fun toString(): String {
        return "streamCode: $streamCode, statusCode: $statusCode, message: $message"
    }
}