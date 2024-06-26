package com.example.composechatsample.log

import com.example.composechatsample.log.Priority.ASSERT
import com.example.composechatsample.log.Priority.ERROR
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

open class KotlinStreamLogger(
    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss''SSS", Locale.ENGLISH),
    private val now: () -> Long = { System.currentTimeMillis() }
) : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        val now = dateFormat.format(now())
        val thread = Thread.currentThread().run { "$name:$id" }
        val composed = "$now ($thread) [${priority.stringify()}/$tag]: $message"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed
        when (priority) {
            ERROR, ASSERT -> System.err.println(finalMessage)
            else -> println(finalMessage)
        }
    }
}