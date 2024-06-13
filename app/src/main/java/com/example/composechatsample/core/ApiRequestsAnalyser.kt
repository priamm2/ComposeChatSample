package com.example.composechatsample.core

public interface ApiRequestsAnalyser {

    public fun registerRequest(requestName: String, data: Map<String, String>)

    public fun dumpRequestByName(requestName: String): String

    public fun dumpAll(): String

    public fun clearAll()

    public fun clearRequestContaining(queryText: String)

    public fun countRequestContaining(requestName: String): Int

    public fun countAllRequests(): Int

    public companion object {
        private var instance: ApiRequestsAnalyser? = null

        public fun get(): ApiRequestsAnalyser = instance ?: ApiRequestsDumper().also { dumper ->
            instance = dumper
        }

        public fun isInitialized(): Boolean = instance != null
    }
}