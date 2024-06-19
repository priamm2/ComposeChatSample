package com.example.composechatsample.screen.messages

import androidx.compose.ui.graphics.painter.Painter
import com.example.composechatsample.core.models.User

data class UserReactionItemState(
    public val user: User,
    public val painter: Painter,
    public val type: String,
)