package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.QueryMembersRequest
import com.example.composechatsample.core.models.requests.SearchMessagesRequest
import com.example.composechatsample.core.models.requests.SyncHistoryRequest
import com.example.composechatsample.core.models.response.QueryMembersResponse
import com.example.composechatsample.core.models.response.SearchMessagesResponse
import com.example.composechatsample.core.models.response.SyncHistoryResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface GeneralApi {
    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>

    @POST("/sync")
    fun getSyncHistory(
        @Body body: SyncHistoryRequest,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<SyncHistoryResponse>

    @GET("/search")
    fun searchMessages(
        @UrlQueryPayload @Query("payload") payload: SearchMessagesRequest,
    ): RetrofitCall<SearchMessagesResponse>

    @GET("/members")
    fun queryMembers(
        @UrlQueryPayload @Query("payload") payload: QueryMembersRequest,
    ): RetrofitCall<QueryMembersResponse>
}