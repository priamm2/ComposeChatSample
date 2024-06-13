package com.example.composechatsample.core

public interface ProgressCallback {

    public fun onSuccess(url: String?)

    public fun onError(error: Error)

    public fun onProgress(bytesUploaded: Long, totalBytes: Long)
}