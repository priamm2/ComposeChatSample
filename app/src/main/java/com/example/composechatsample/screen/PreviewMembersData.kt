package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Member

internal object PreviewMembersData {

    private val member1: Member = Member(user = PreviewUserData.user1)
    private val member2: Member = Member(user = PreviewUserData.user2)
    private val member3: Member = Member(user = PreviewUserData.user3)
    private val member4: Member = Member(user = PreviewUserData.user4)

    val oneMember: List<Member> = listOf(member1)

    val manyMembers: List<Member> = listOf(
        member1,
        member2,
        member3,
        member4,
    )
}