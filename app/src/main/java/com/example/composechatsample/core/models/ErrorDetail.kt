package com.example.composechatsample.core.models

data class ErrorDetail(
    val code: Int,
    val messages: List<String>,
)