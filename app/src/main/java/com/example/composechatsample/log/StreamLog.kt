package com.example.composechatsample.log

import com.example.composechatsample.log.Priority.ASSERT
import com.example.composechatsample.log.Priority.DEBUG
import com.example.composechatsample.log.Priority.ERROR
import com.example.composechatsample.log.Priority.INFO
import com.example.composechatsample.log.Priority.VERBOSE
import com.example.composechatsample.log.Priority.WARN

public object StreamLog {

    @JvmStatic
    public var isInstalled: Boolean = false
        private set

    @Volatile
    @PublishedApi
    internal var internalLogger: StreamLogger = ErrorStreamLogger
        private set(value) {
            isInstalled = true
            field = value
        }


    @Volatile
    @PublishedApi
    internal var internalValidator: IsLoggableValidator = IsLoggableValidator { priority, _ ->
        priority.level >= ERROR.level
    }
        private set

    @JvmStatic
    public fun install(logger: StreamLogger) {
        synchronized(this) {
            if (isInstalled) {
                e("StreamLog") {
                    "The logger $internalLogger is already installed but you've tried to install a new logger: $logger"
                }
            }
            internalLogger = logger
        }
    }

    @JvmStatic
    public fun unInstall() {
        synchronized(this) {
            internalLogger = SilentStreamLogger
            isInstalled = false
        }
    }

    @JvmStatic
    public fun setValidator(validator: IsLoggableValidator) {
        synchronized(this) {
            internalValidator = validator
        }
    }

    @JvmStatic
    public fun getLogger(tag: String): TaggedLogger = TaggedLogger(tag, internalLogger, internalValidator)

    @JvmStatic
    public inline fun e(tag: String, throwable: Throwable, message: () -> String) {
        if (internalValidator.isLoggable(ERROR, tag)) {
            internalLogger.log(ERROR, tag, message(), throwable)
        }
    }

    @JvmStatic
    public inline fun e(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(ERROR, tag)) {
            internalLogger.log(ERROR, tag, message())
        }
    }

    @JvmStatic
    public inline fun w(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(WARN, tag)) {
            internalLogger.log(WARN, tag, message())
        }
    }

    @JvmStatic
    public inline fun i(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(INFO, tag)) {
            internalLogger.log(INFO, tag, message())
        }
    }

    @JvmStatic
    public inline fun d(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(DEBUG, tag)) {
            internalLogger.log(DEBUG, tag, message())
        }
    }

    @JvmStatic
    public inline fun v(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(VERBOSE, tag)) {
            internalLogger.log(VERBOSE, tag, message())
        }
    }

    @JvmStatic
    public inline fun a(tag: String, message: () -> String) {
        if (internalValidator.isLoggable(ASSERT, tag)) {
            internalLogger.log(ASSERT, tag, message())
        }
    }

    @JvmStatic
    public inline fun log(priority: Priority, tag: String, throwable: Throwable? = null, message: () -> String) {
        if (throwable != null) {
            e(tag, throwable, message)
        } else {
            when (priority) {
                VERBOSE -> v(tag, message)
                DEBUG -> d(tag, message)
                INFO -> i(tag, message)
                WARN -> w(tag, message)
                ERROR -> e(tag, message)
                ASSERT -> a(tag, message)
            }
        }
    }
}

public fun interface IsLoggableValidator {
    public fun isLoggable(priority: Priority, tag: String): Boolean
}

public class TaggedLogger(
    @PublishedApi internal val tag: String,
    @PublishedApi internal val delegate: StreamLogger,
    @PublishedApi internal var validator: IsLoggableValidator,
) {

    public inline fun e(throwable: Throwable, message: () -> String) {
        if (validator.isLoggable(ERROR, tag)) {
            delegate.log(ERROR, tag, message(), throwable)
        }
    }

    public inline fun e(message: () -> String) {
        if (validator.isLoggable(ERROR, tag)) {
            delegate.log(ERROR, tag, message())
        }
    }

    public inline fun w(message: () -> String) {
        if (validator.isLoggable(WARN, tag)) {
            delegate.log(WARN, tag, message())
        }
    }

    public inline fun i(message: () -> String) {
        if (validator.isLoggable(INFO, tag)) {
            delegate.log(INFO, tag, message())
        }
    }

    public inline fun d(message: () -> String) {
        if (validator.isLoggable(DEBUG, tag)) {
            delegate.log(DEBUG, tag, message())
        }
    }

    public inline fun v(message: () -> String) {
        if (validator.isLoggable(VERBOSE, tag)) {
            delegate.log(VERBOSE, tag, message())
        }
    }
}