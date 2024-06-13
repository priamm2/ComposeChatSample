package com.example.composechatsample.core

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOT_FOUND = "not found"
private const val NOT_FOUND_INT = -1

internal class ApiRequestsDumper(
    private val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()),
) : ApiRequestsAnalyser {

    private val requestsDataMap: MutableMap<String, List<RequestData>> = mutableMapOf()

    override fun registerRequest(requestName: String, data: Map<String, String>) {
        synchronized(this) {
            val requestData = RequestData(requestName, Date(), data)
            requestsDataMap[requestName] = (requestsDataMap[requestName] ?: emptyList()) + requestData
        }
    }

    override fun dumpRequestByName(requestName: String): String {
        return requestsDataMap[requestName]
            ?.toHumanReadableStringBuilder()
            ?.toString()
            ?: NOT_FOUND
    }

    override fun dumpAll(): String {
        return buildString {
            requestsDataMap.values.forEach { requestDataList ->
                append(requestDataList.toHumanReadableStringBuilder())
                appendLine()
            }
        }
    }

    override fun clearAll() {
        requestsDataMap.clear()
    }

    override fun clearRequestContaining(queryText: String) {
        synchronized(this) {
            val keys = requestsDataMap.keys.filter { key ->
                key.contains(queryText)
            }
            keys.forEach(requestsDataMap::remove)
        }
    }

    override fun countRequestContaining(requestName: String): Int {
        val matchKey = requestsDataMap.keys.find { key ->
            key.contains(requestName)
        }

        return requestsDataMap[matchKey]?.count() ?: NOT_FOUND_INT
    }

    override fun countAllRequests(): Int {
        return requestsDataMap.values.fold(0) { acc, list -> acc + list.count() }
    }

    private fun List<RequestData>.toHumanReadableStringBuilder(): StringBuilder {
        val dataList = this
        val requestName = first().name
        val count = count()

        val extraDataBuilder = StringBuilder().apply {
            dataList.forEachIndexed { i, requestData ->
                val time = dateFormat.format(requestData.time)
                val params = requestData.extraData.entries.joinToString { (key, value) -> "$key - $value" }
                appendLine("Call $i. Time: $time. Params: $params")
            }
        }

        return StringBuilder().apply {
            appendLine("Request: $requestName. Count: $count")
            append(extraDataBuilder)
        }
    }
}