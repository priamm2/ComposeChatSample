package com.example.composechatsample.core

import com.example.composechatsample.core.api.AnonymousApi
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

@AnonymousApi
internal interface FileDownloadApi {

    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): RetrofitCall<ResponseBody>
}