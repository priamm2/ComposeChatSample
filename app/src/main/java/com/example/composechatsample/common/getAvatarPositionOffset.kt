package com.example.composechatsample.common

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.composechatsample.ui.theme.StreamDimens

internal fun getAvatarPositionOffset(
    dimens: StreamDimens,
    userPosition: Int,
    memberCount: Int,
): DpOffset {
    val center = DpOffset(0.dp, 0.dp)
    if (memberCount <= 2) return center

    return when (userPosition) {
        0 -> DpOffset(
            dimens.groupAvatarInitialsXOffset,
            dimens.groupAvatarInitialsYOffset,
        )
        1 -> {
            if (memberCount == 3) {
                center
            } else {
                DpOffset(
                    -dimens.groupAvatarInitialsXOffset,
                    dimens.groupAvatarInitialsYOffset,
                )
            }
        }
        2 -> DpOffset(
            dimens.groupAvatarInitialsXOffset,
            -dimens.groupAvatarInitialsYOffset,
        )
        3 -> DpOffset(
            -dimens.groupAvatarInitialsXOffset,
            -dimens.groupAvatarInitialsYOffset,
        )
        else -> center
    }
}