package com.example.composechatsample.core

import com.example.composechatsample.core.api.AnonymousApi
import com.example.composechatsample.core.models.requests.GuestUserRequest
import com.example.composechatsample.core.models.response.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

@AnonymousApi
internal interface GuestApi {

    @POST("/guest")
    fun getGuestUser(
        @Body body: GuestUserRequest,
    ): RetrofitCall<TokenResponse>
}