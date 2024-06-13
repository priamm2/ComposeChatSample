package com.example.composechatsample.helper

import android.os.Build
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresApi
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.example.composechatsample.R
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.ZoneId

public interface DateFormatter {
    public fun formatDate(date: Date?): String
    public fun formatTime(date: Date?): String
    public fun formatRelativeTime(date: Date?): String

    public companion object {
        @JvmStatic
        @JvmOverloads
        public fun from(context: Context, locale: Locale = Locale.getDefault()): DateFormatter = DefaultDateFormatter(
            context,
            locale,
        )
    }
}

internal class DefaultDateFormatter(
    private val dateContext: DateContext,
    private val locale: Locale,
) : DateFormatter {

    constructor(context: Context, locale: Locale) : this(DefaultDateContext(context, locale), locale)

    private companion object {
        const val TIME_FORMAT_12H = "h:mm a"
        const val TIME_FORMAT_24H = "HH:mm"
        const val DATE_FORMAT_DAY_OF_WEEK = "EEEE"
    }

    private val timeFormatter12h: SimpleDateFormat = SimpleDateFormat(TIME_FORMAT_12H, locale)
    private val timeFormatter24h: SimpleDateFormat = SimpleDateFormat(TIME_FORMAT_24H, locale)
    private val dateFormatterDayOfWeek: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_DAY_OF_WEEK, locale)
    private val dateFormatterFullDate: SimpleDateFormat
        get() = SimpleDateFormat(dateContext.dateTimePattern(), locale)

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter12hNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(TIME_FORMAT_12H)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter24hNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(TIME_FORMAT_24H)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatterDayOfWeekNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(DATE_FORMAT_DAY_OF_WEEK)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    private val dateFormatterFullDateNew: DateTimeFormatter
        @SuppressLint("NewApi")
        get() = DateTimeFormatter.ofPattern(dateContext.dateTimePattern())
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())

    override fun formatDate(date: Date?): String {
        date ?: return ""

        return when {
            date.isToday() -> formatTime(date)
            date.isYesterday() -> dateContext.yesterdayString()
            date.isWithinLastWeek() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateFormatterDayOfWeekNew.format(date.toInstant())
                } else {
                    dateFormatterDayOfWeek.format(date)
                }
            }
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateFormatterFullDateNew.format(date.toInstant())
            } else {
                dateFormatterFullDate.format(date)
            }
        }
    }

    override fun formatTime(date: Date?): String {
        date ?: return ""

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateFormat = if (dateContext.is24Hour()) timeFormatter24hNew else timeFormatter12hNew
            dateFormat.format(date.toInstant())
        } else {
            val dateFormat = if (dateContext.is24Hour()) timeFormatter24h else timeFormatter12h
            dateFormat.format(date)
        }
    }

    override fun formatRelativeTime(date: Date?): String {
        date ?: return ""

        return DateUtils.getRelativeDateTimeString(
            dateContext.context(),
            date.time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,
            0,
        ).toString()
    }

    private fun Date.isToday(): Boolean {
        val calendar1 = Calendar.getInstance().also { it.time = dateContext.now() }
        val calendar2 = Calendar.getInstance().also { it.time = this }

        return (calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]) &&
            calendar1[Calendar.DAY_OF_YEAR] == calendar2[Calendar.DAY_OF_YEAR]
    }

    private fun Date.isYesterday(): Boolean {
        return Date(time + DateUtils.DAY_IN_MILLIS).isToday()
    }

    private fun Date.isWithinLastWeek(): Boolean {
        return isWithinDays(DAYS_IN_WEEK - 1)
    }

    private fun Date.isWithinDays(days: Int): Boolean {
        val calendar: Calendar = Calendar.getInstance().also { it.time = this }

        val currentDate = dateContext.now()
        val start: Calendar = Calendar.getInstance().also {
            it.time = currentDate
            it.add(Calendar.DAY_OF_YEAR, -days)
        }
        val end: Calendar = Calendar.getInstance().also { it.time = currentDate }

        return calendar.isBeforeDay(end) && !calendar.isBeforeDay(start)
    }

    private fun Calendar.isBeforeDay(calendar: Calendar): Boolean {
        return when {
            this[Calendar.YEAR] < calendar[Calendar.YEAR] -> true
            this[Calendar.YEAR] > calendar[Calendar.YEAR] -> false
            else -> this[Calendar.DAY_OF_YEAR] < calendar[Calendar.DAY_OF_YEAR]
        }
    }

    interface DateContext {
        fun now(): Date
        fun yesterdayString(): String
        fun is24Hour(): Boolean
        fun dateTimePattern(): String
        fun context(): Context
    }

    private class DefaultDateContext(
        private val context: Context,
        private val locale: Locale,
    ) : DateContext {

        private val dateTimePatternLazy by lazy {
            DateFormat.getBestDateTimePattern(locale, "yy MM dd")
        }

        override fun now(): Date = Date()

        override fun yesterdayString(): String {
            return context.getString(R.string.stream_ui_yesterday)
        }

        override fun is24Hour(): Boolean {
            return DateFormat.is24HourFormat(context)
        }

        override fun dateTimePattern(): String {
            return dateTimePatternLazy
        }

        override fun context(): Context {
            return context
        }
    }
}

private const val DAYS_IN_WEEK = 7