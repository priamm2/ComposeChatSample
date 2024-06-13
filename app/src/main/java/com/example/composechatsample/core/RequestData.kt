package com.example.composechatsample.core

import java.util.Date

internal data class RequestData(
    val name: String,
    val time: Date,
    val extraData: Map<String, String>,
)