package com.example.composechatsample.screen.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.composechatsample.R

internal object PreviewReactionOptionData {

    @Composable
    fun reactionOption1() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
        type = "like",
    )

    @Composable
    fun reactionOption2() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_love_selected),
        type = "love",
    )

    @Composable
    fun reactionOption3() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_wut),
        type = "wow",
    )

    @Composable
    fun reactionOption4() = ReactionOptionItemState(
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_down_selected),
        type = "sad",
    )

    @Composable
    fun oneReaction(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
    )

    @Composable
    fun manyReactions(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
        reactionOption2(),
        reactionOption3(),
        reactionOption4(),
    )
}