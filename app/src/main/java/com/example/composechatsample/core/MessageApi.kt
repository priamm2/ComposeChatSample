package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.PartialUpdateMessageRequest
import com.example.composechatsample.core.models.requests.ReactionRequest
import com.example.composechatsample.core.models.requests.SendMessageRequest
import com.example.composechatsample.core.models.requests.TranslateMessageRequest
import com.example.composechatsample.core.models.requests.UpdateMessageRequest
import com.example.composechatsample.core.models.response.MessageResponse
import com.example.composechatsample.core.models.response.MessagesResponse
import com.example.composechatsample.core.models.response.ReactionResponse
import com.example.composechatsample.core.models.response.ReactionsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface MessageApi {

    @POST("/channels/{type}/{id}/message")
    fun sendMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body message: SendMessageRequest,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}")
    fun getMessage(@Path("id") messageId: String): RetrofitCall<MessageResponse>

    @POST("/messages/{id}")
    fun updateMessage(
        @Path("id") messageId: String,
        @Body message: UpdateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @PUT("/messages/{id}")
    fun partialUpdateMessage(
        @Path("id") messageId: String,
        @Body body: PartialUpdateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @DELETE("/messages/{id}")
    fun deleteMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.HARD_DELETE) hard: Boolean?,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/action")
    fun sendAction(
        @Path("id") messageId: String,
        @Body request: SendActionRequest,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/reaction")
    fun sendReaction(
        @Path("id") messageId: String,
        @Body request: ReactionRequest,
    ): RetrofitCall<ReactionResponse>

    @DELETE("/messages/{id}/reaction/{type}")
    fun deleteReaction(
        @Path("id") messageId: String,
        @Path("type") reactionType: String,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}/reactions")
    fun getReactions(
        @Path("id") messageId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): RetrofitCall<ReactionsResponse>

    @POST("/messages/{messageId}/translate")
    fun translate(
        @Path("messageId") messageId: String,
        @Body request: TranslateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{parent_id}/replies")
    fun getReplies(
        @Path("parent_id") messageId: String,
        @Query("limit") limit: Int,
    ): RetrofitCall<MessagesResponse>

    @GET("/messages/{parent_id}/replies?sort=[{\"field\":\"created_at\",\"direction\":1}]")
    fun getNewerReplies(
        @Path("parent_id") parentId: String,
        @Query("limit") limit: Int,
        @Query("id_gt") lastId: String?,
    ): RetrofitCall<MessagesResponse>

    @GET("/messages/{parent_id}/replies")
    fun getRepliesMore(
        @Path("parent_id") messageId: String,
        @Query("limit") limit: Int,
        @Query("id_lt") firstId: String,
    ): RetrofitCall<MessagesResponse>
}