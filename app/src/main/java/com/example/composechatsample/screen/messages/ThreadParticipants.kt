package com.example.composechatsample.screen.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.composechatsample.core.initials
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.components.Avatar
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.MessageAlignment

@Composable
public fun ThreadParticipants(
    participants: List<User>,
    alignment: MessageAlignment,
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke = BorderStroke(width = 1.dp, color = ChatTheme.colors.appBackground),
    participantsLimit: Int = DefaultParticipantsLimit,
) {
    Box(modifier) {
        val participantsToShow = participants.take(participantsLimit).let {
            if (alignment == MessageAlignment.End) {
                it.reversed()
            } else {
                it
            }
        }
        val itemSize = ChatTheme.dimens.threadParticipantItemSize

        participantsToShow.forEachIndexed { index, user ->
            val itemPadding = Modifier.padding(start = (index * (itemSize.value / 2)).dp)

            val itemPosition = if (alignment == MessageAlignment.Start) {
                participantsLimit - index
            } else {
                index + 1
            }.toFloat()

            Avatar(
                modifier = itemPadding
                    .zIndex(itemPosition)
                    .size(itemSize)
                    .border(border = borderStroke, shape = ChatTheme.shapes.avatar),
                imageUrl = user.image,
                initials = user.initials,
                textStyle = ChatTheme.typography.captionBold.copy(fontSize = 7.sp),
            )
        }
    }
}

private const val DefaultParticipantsLimit = 4