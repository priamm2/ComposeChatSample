package com.example.composechatsample.core.errors

public data class ErrorDetail(
    public val code: Int,
    public val messages: List<String>,
)