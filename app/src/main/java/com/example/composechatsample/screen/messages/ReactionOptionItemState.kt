package com.example.composechatsample.screen.messages

import androidx.compose.ui.graphics.painter.Painter

public data class ReactionOptionItemState(
    public val painter: Painter,
    public val type: String,
)