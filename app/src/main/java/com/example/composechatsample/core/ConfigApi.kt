package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.response.AppSettingsResponse
import retrofit2.http.GET

@AuthenticatedApi
internal interface ConfigApi {

    @GET("/app")
    fun getAppSettings(): RetrofitCall<AppSettingsResponse>
}