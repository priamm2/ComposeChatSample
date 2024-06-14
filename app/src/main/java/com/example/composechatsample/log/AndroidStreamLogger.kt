package com.example.composechatsample.log

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast

public class AndroidStreamLogger constructor(
    private val maxTagLength: Int = DEFAULT_MAX_TAG_LENGTH,
) : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {

        val androidPriority = priority.toAndroidPriority()
        val androidTag = tag.takeIf { it.length > maxTagLength && !isNougatOrHigher() }
            ?.substring(0, maxTagLength)
            ?: tag

        val thread = Thread.currentThread().run { "$name:$id" }
        val composed = "($thread) $message"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed

        Log.println(androidPriority, androidTag, finalMessage)
    }

    private fun Priority.toAndroidPriority(): Int {
        return when (this) {
            Priority.VERBOSE -> Log.VERBOSE
            Priority.DEBUG -> Log.DEBUG
            Priority.INFO -> Log.INFO
            Priority.WARN -> Log.WARN
            Priority.ERROR -> Log.ERROR
            Priority.ASSERT -> Log.ASSERT
            else -> Log.ERROR
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    private fun isNougatOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    public companion object {
        private val Application.isDebuggableApp: Boolean
            get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        public fun installOnDebuggableApp(
            application: Application,
            minPriority: Priority = Priority.DEBUG,
            maxTagLength: Int = DEFAULT_MAX_TAG_LENGTH,
        ) {
            if (!StreamLog.isInstalled && application.isDebuggableApp) {
                StreamLog.setValidator { priority, _ -> priority.level >= minPriority.level }
                StreamLog.install(AndroidStreamLogger(maxTagLength = maxTagLength))
            }
        }

        public fun install(minPriority: Priority = Priority.DEBUG, maxTagLength: Int = DEFAULT_MAX_TAG_LENGTH) {
            StreamLog.setValidator { priority, _ -> priority.level >= minPriority.level }
            StreamLog.install(AndroidStreamLogger(maxTagLength = maxTagLength))
        }

        internal const val DEFAULT_MAX_TAG_LENGTH = 23
    }
}