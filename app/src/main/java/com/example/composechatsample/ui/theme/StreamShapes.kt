package com.example.composechatsample.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
public data class StreamShapes(
    public val avatar: Shape,
    public val myMessageBubble: Shape,
    public val otherMessageBubble: Shape,
    public val inputField: Shape,
    public val attachment: Shape,
    public val imageThumbnail: Shape,
    public val bottomSheet: Shape,
    public val suggestionList: Shape,
    public val attachmentSiteLabel: Shape,
    public val header: Shape,
    public val quotedAttachment: Shape,
) {
    public companion object {

        public fun defaultShapes(): StreamShapes = StreamShapes(
            avatar = CircleShape,
            myMessageBubble = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp),
            otherMessageBubble = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
            inputField = RoundedCornerShape(24.dp),
            attachment = RoundedCornerShape(16.dp),
            imageThumbnail = RoundedCornerShape(8.dp),
            bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            suggestionList = RoundedCornerShape(16.dp),
            attachmentSiteLabel = RoundedCornerShape(topEnd = 14.dp),
            header = RectangleShape,
            quotedAttachment = RoundedCornerShape(4.dp),
        )
    }
}