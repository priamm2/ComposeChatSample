package com.example.composechatsample.screen.messages;

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

public class MessageOptionItemState(
    @StringRes public val title: Int,
    public val titleColor: Color,
    public val iconPainter: Painter,
    public val iconColor: Color,
    public val action: MessageAction,
)