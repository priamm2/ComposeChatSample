package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.BanUserRequest
import com.example.composechatsample.core.models.requests.FlagRequest
import com.example.composechatsample.core.models.requests.MuteChannelRequest
import com.example.composechatsample.core.models.requests.MuteUserRequest
import com.example.composechatsample.core.models.requests.QueryBannedUsersRequest
import com.example.composechatsample.core.models.response.CompletableResponse
import com.example.composechatsample.core.models.response.FlagResponse
import com.example.composechatsample.core.models.response.MuteUserResponse
import com.example.composechatsample.core.models.response.QueryBannedUsersResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface ModerationApi {

    @POST("/moderation/mute")
    fun muteUser(@Body body: MuteUserRequest): RetrofitCall<MuteUserResponse>

    @POST("/moderation/unmute")
    fun unmuteUser(@Body body: MuteUserRequest): RetrofitCall<CompletableResponse>

    @POST("/moderation/mute/channel")
    fun muteChannel(@Body body: MuteChannelRequest): RetrofitCall<CompletableResponse>

    @POST("/moderation/unmute/channel")
    fun unmuteChannel(@Body body: MuteChannelRequest): RetrofitCall<CompletableResponse>

    @POST("/moderation/flag")
    fun flag(@Body body: FlagRequest): RetrofitCall<FlagResponse>

    @POST("/moderation/unflag")
    fun unflag(@Body body: Map<String, String>): RetrofitCall<FlagResponse>

    @POST("/moderation/ban")
    fun banUser(@Body body: BanUserRequest): RetrofitCall<CompletableResponse>

    @DELETE("/moderation/ban")
    fun unbanUser(
        @Query("target_user_id") targetUserId: String,
        @Query("type") channelType: String,
        @Query("id") channelId: String,
        @Query("shadow") shadow: Boolean,
    ): RetrofitCall<CompletableResponse>

    @GET("/query_banned_users")
    fun queryBannedUsers(
        @UrlQueryPayload @Query("payload") payload: QueryBannedUsersRequest,
    ): RetrofitCall<QueryBannedUsersResponse>
}