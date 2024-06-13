package com.example.composechatsample.log

import java.io.PrintWriter
import java.io.StringWriter

private const val INITIAL_BUFFER_SIZE = 256

public fun Thread.stringify(): String {
    return "$name:$id"
}

public fun Priority.stringify(): String = when (this) {
    Priority.VERBOSE -> "V"
    Priority.DEBUG -> "D"
    Priority.INFO -> "I"
    Priority.WARN -> "W"
    Priority.ERROR -> "E"
    Priority.ASSERT -> "E"
}

public fun Throwable.stringify(): String {
    // Don't replace this with Log.getStackTraceString() - it hides
    // UnknownHostException, which is not what we want.
    val sw = StringWriter(INITIAL_BUFFER_SIZE)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}