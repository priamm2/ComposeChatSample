package com.example.composechatsample.log

public inline fun Any.streamLog(
    priority: Priority = Priority.DEBUG,
    tag: String? = null,
    throwable: Throwable? = null,
    message: () -> String,
) {
    val tagOrCaller = tag ?: outerClassSimpleTagName()
    StreamLog.log(priority, tagOrCaller, throwable, message)
}

public fun Any.taggedLogger(
    tag: String? = null,
): Lazy<TaggedLogger> {
    val tagOrCaller = tag ?: outerClassSimpleTagName()
    return lazy { StreamLog.getLogger(tagOrCaller) }
}

@PublishedApi
internal fun Any.outerClassSimpleTagName(): String {
    val javaClass = this::class.java
    val fullClassName = javaClass.name
    val outerClassName = fullClassName.substringBefore('$')
    val simplerOuterClassName = outerClassName.substringAfterLast('.')
    return if (simplerOuterClassName.isEmpty()) {
        fullClassName
    } else {
        simplerOuterClassName.removeSuffix("Kt")
    }
}