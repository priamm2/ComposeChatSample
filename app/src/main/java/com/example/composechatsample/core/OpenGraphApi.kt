package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.dto.AttachmentDto
import retrofit2.http.GET
import retrofit2.http.Query

@AuthenticatedApi
internal interface OpenGraphApi {

    @GET("/og")
    fun get(@Query(QueryParams.URL) url: String): RetrofitCall<AttachmentDto>
}