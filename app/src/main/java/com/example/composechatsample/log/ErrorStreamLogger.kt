package com.example.composechatsample.log

import com.example.composechatsample.log.Priority.ASSERT
import com.example.composechatsample.log.Priority.ERROR

internal object ErrorStreamLogger : KotlinStreamLogger() {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        when (priority) {
            ERROR, ASSERT -> super.log(priority, tag, message, throwable)
            else -> { /* NO-OP */ }
        }
    }
}