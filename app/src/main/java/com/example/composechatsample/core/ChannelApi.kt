package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.AcceptInviteRequest
import com.example.composechatsample.core.models.requests.AddMembersRequest
import com.example.composechatsample.core.models.response.ChannelResponse
import com.example.composechatsample.core.models.response.CompletableResponse
import com.example.composechatsample.core.models.response.EventResponse
import com.example.composechatsample.core.models.requests.HideChannelRequest
import com.example.composechatsample.core.models.requests.InviteMembersRequest
import com.example.composechatsample.core.models.requests.MarkReadRequest
import com.example.composechatsample.core.models.requests.MarkUnreadRequest
import com.example.composechatsample.core.models.response.MessagesResponse
import com.example.composechatsample.core.models.requests.PinnedMessagesRequest
import com.example.composechatsample.core.models.response.QueryChannelsResponse
import com.example.composechatsample.core.models.requests.RejectInviteRequest
import com.example.composechatsample.core.models.requests.RemoveMembersRequest
import com.example.composechatsample.core.models.SendEventRequest
import com.example.composechatsample.core.models.TruncateChannelRequest
import com.example.composechatsample.core.models.UpdateChannelPartialRequest
import com.example.composechatsample.core.models.requests.QueryChannelRequest
import com.example.composechatsample.core.models.requests.QueryChannelsRequest
import com.example.composechatsample.core.models.requests.UpdateChannelRequest
import com.example.composechatsample.core.models.requests.UpdateCooldownRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface ChannelApi {

    @POST("/channels")
    fun queryChannels(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelsRequest,
    ): RetrofitCall<QueryChannelsResponse>

    @POST("/channels/{type}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/read")
    fun markAllRead(
        @Body map: Map<String, String> = emptyMap(),
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}")
    fun updateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @PATCH("/channels/{type}/{id}")
    @JvmSuppressWildcards
    fun updateChannelPartial(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelPartialRequest,
    ): RetrofitCall<ChannelResponse>

    @PATCH("/channels/{type}/{id}")
    @JvmSuppressWildcards
    fun updateCooldown(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateCooldownRequest,
    ): RetrofitCall<ChannelResponse>

    @DELETE("/channels/{type}/{id}")
    fun deleteChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun acceptInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: AcceptInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun rejectInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: RejectInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun addMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: AddMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun removeMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: RemoveMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun inviteMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: InviteMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: SendEventRequest,
    ): RetrofitCall<EventResponse>

    @POST("/channels/{type}/{id}/hide")
    fun hideChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: HideChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/truncate")
    fun truncateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: TruncateChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/read")
    fun markRead(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkReadRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/unread")
    fun markUnread(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkUnreadRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/show")
    @JvmSuppressWildcards
    fun showChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>

    @GET("/channels/{type}/{id}/pinned_messages")
    fun getPinnedMessages(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @UrlQueryPayload @Query("payload") payload: PinnedMessagesRequest,
    ): RetrofitCall<MessagesResponse>
}