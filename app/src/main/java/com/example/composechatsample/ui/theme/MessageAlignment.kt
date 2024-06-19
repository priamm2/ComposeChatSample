package com.example.composechatsample.ui.theme

import androidx.compose.ui.Alignment

public enum class MessageAlignment(
    public val itemAlignment: Alignment,
    public val contentAlignment: Alignment.Horizontal,
) {

    Start(Alignment.CenterStart, Alignment.Start),
    End(Alignment.CenterEnd, Alignment.End),
}