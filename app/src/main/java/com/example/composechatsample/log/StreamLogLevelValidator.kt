package com.example.composechatsample.log

internal class StreamLogLevelValidator(
    private val logLevel: ChatLogLevel,
) : IsLoggableValidator {

    override fun isLoggable(priority: Priority, tag: String): Boolean {
        return when (logLevel) {
            ChatLogLevel.NOTHING -> false
            ChatLogLevel.ALL -> true
            ChatLogLevel.DEBUG -> priority.level >= Priority.DEBUG.level
            ChatLogLevel.WARN -> priority.level >= Priority.WARN.level
            ChatLogLevel.ERROR -> priority.level >= Priority.ERROR.level
        }
    }
}