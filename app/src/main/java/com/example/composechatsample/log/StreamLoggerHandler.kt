package com.example.composechatsample.log

internal class StreamLoggerHandler(
    private val handler: ChatLoggerHandler?,
) : StreamLogger {


    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        handler?.run {
            when (priority) {
                Priority.VERBOSE -> logV(tag, message)
                Priority.DEBUG -> logD(tag, message)
                Priority.INFO -> logI(tag, message)
                Priority.WARN -> logW(tag, message)
                Priority.ERROR, Priority.ASSERT -> when (throwable) {
                    null -> logE(tag, message)
                    else -> logE(tag, message, throwable)
                }
            }
        }
    }
}