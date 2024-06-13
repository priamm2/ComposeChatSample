package com.example.composechatsample.log

public interface StreamLogger {

    public fun log(priority: Priority, tag: String, message: String, throwable: Throwable? = null)
}

public enum class Priority(
    public val level: Int,
) {
    VERBOSE(level = 2),
    DEBUG(level = 3),
    INFO(level = 4),
    WARN(level = 5),
    ERROR(level = 6),
    ASSERT(level = 7),
}

public object SilentStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) { /* no-op */
    }
}