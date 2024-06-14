package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.PartialUpdateUsersRequest
import com.example.composechatsample.core.models.requests.QueryUsersRequest
import com.example.composechatsample.core.models.requests.UpdateUsersRequest
import com.example.composechatsample.core.models.response.UpdateUsersResponse
import com.example.composechatsample.core.models.response.UsersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface UserApi {
    @POST("/users")
    fun updateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: UpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @PATCH("/users")
    @JvmSuppressWildcards
    fun partialUpdateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: PartialUpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @GET("/users")
    fun queryUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryUsersRequest,
    ): RetrofitCall<UsersResponse>
}