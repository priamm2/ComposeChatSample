package com.example.composechatsample.screen.channels;

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

public class ChannelOptionState(
    public val title: String,
    public val titleColor: Color,
    public val iconPainter: Painter,
    public val iconColor: Color,
    public val action: ChannelAction,
)