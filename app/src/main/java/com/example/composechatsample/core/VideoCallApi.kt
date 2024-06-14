package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.VideoCallCreateRequest
import com.example.composechatsample.core.models.requests.VideoCallTokenRequest
import com.example.composechatsample.core.models.response.CreateVideoCallResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

@AuthenticatedApi
internal interface VideoCallApi {

    @POST("/channels/{channelType}/{channelId}/call")
    fun createCall(
        @Path("channelType") channelType: String,
        @Path("channelId") channelId: String,
        @Body request: VideoCallCreateRequest,
    ): RetrofitCall<CreateVideoCallResponse>

    @POST("/calls/{callId}")
    fun getCallToken(
        @Path("callId") callId: String,
        @Body request: VideoCallTokenRequest,
    ): RetrofitCall<VideoCallTokenResponse>
}