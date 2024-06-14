package com.example.composechatsample.core

import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.models.requests.AddDeviceRequest
import com.example.composechatsample.core.models.response.CompletableResponse
import com.example.composechatsample.core.models.response.DevicesResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface DeviceApi {

    @GET("/devices")
    fun getDevices(): RetrofitCall<DevicesResponse>

    @POST("devices")
    fun addDevices(@Body request: AddDeviceRequest): RetrofitCall<CompletableResponse>

    @DELETE("/devices")
    fun deleteDevice(@Query("id") deviceId: String): RetrofitCall<CompletableResponse>
}