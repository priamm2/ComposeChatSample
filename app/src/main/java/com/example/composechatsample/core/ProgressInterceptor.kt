package com.example.composechatsample.core

import com.example.composechatsample.core.models.ProgressRequestBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class ProgressInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val progressCallback = request.tag(ProgressCallback::class.java)
        if (progressCallback != null) {
            return chain.proceed(wrapRequest(request, progressCallback))
        }

        return chain.proceed(request)
    }

    private fun wrapRequest(request: Request, progressCallback: ProgressCallback): Request {
        return request.newBuilder()
            .post(ProgressRequestBody(request.body!!, progressCallback))
            .build()
    }
}