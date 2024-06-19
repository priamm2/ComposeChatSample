package com.example.composechatsample

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.messages.UserReactionItemState

internal object PreviewUserReactionData {

    @Composable
    fun user1Reaction() = UserReactionItemState(
        user = PreviewUserData.user1,
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_up),
        type = "like",
    )

    @Composable
    fun user2Reaction() = UserReactionItemState(
        user = PreviewUserData.user2,
        painter = painterResource(R.drawable.stream_compose_ic_reaction_love_selected),
        type = "love",
    )

    @Composable
    fun user3Reaction() = UserReactionItemState(
        user = PreviewUserData.user3,
        painter = painterResource(R.drawable.stream_compose_ic_reaction_wut),
        type = "wow",
    )

    @Composable
    fun user4Reaction() = UserReactionItemState(
        user = PreviewUserData.user4,
        painter = painterResource(R.drawable.stream_compose_ic_reaction_thumbs_down_selected),
        type = "sad",
    )

    @Composable
    fun oneUserReaction() = listOf(
        user1Reaction(),
    )

    @Composable
    fun manyUserReactions() = listOf(
        user1Reaction(),
        user2Reaction(),
        user3Reaction(),
        user4Reaction(),
    )
}