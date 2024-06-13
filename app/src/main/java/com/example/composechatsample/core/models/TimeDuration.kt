package com.example.composechatsample.core.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

public class TimeDuration private constructor(
    private val duration: Duration,
) : Comparable<TimeDuration> {

    public val millis: Long get() = duration.inWholeMilliseconds

    public val seconds: Long get() = duration.inWholeSeconds

    public val minutes: Long get() = duration.inWholeMinutes

    public val hours: Long get() = duration.inWholeHours

    public val days: Long get() = duration.inWholeDays

    override fun compareTo(other: TimeDuration): Int {
        return duration.compareTo(other.duration)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeDuration) return false
        return duration == other.duration
    }

    override fun hashCode(): Int {
        return duration.hashCode()
    }

    override fun toString(): String {
        return duration.toString()
    }

    public companion object {

        public fun millis(millis: Long): TimeDuration {
            return TimeDuration(millis.milliseconds)
        }

        public fun seconds(seconds: Int): TimeDuration {
            return TimeDuration(seconds.seconds)
        }

        public fun minutes(minutes: Int): TimeDuration {
            return TimeDuration(minutes.minutes)
        }

        public fun hours(hours: Int): TimeDuration {
            return TimeDuration(hours.hours)
        }
        
        public fun days(days: Int): TimeDuration {
            return TimeDuration(days.days)
        }
    }
}
