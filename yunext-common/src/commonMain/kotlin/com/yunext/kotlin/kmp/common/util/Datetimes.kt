package com.yunext.kotlin.kmp.common.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun currentTime() = Clock.System.now().toEpochMilliseconds()

//fun debugWuYin() = WuYin().test("徵羽")


interface HDDateTimeScope {
    fun Long.toStr(pattern: String = hdFormatPattern): String
    fun String.toTime(pattern: String = hdFormatPattern): Long
    fun LocalDateTime.toInstantCustom(timeZone: TimeZone = hdTimeZone): Instant
    fun Instant.toLocalDateTimeCustom(timeZone: TimeZone = hdTimeZone): LocalDateTime

    fun LocalDateTime.modifyYear(year: Int): LocalDateTime
    fun LocalDateTime.modifyMonth(month: Int): LocalDateTime
    fun LocalDateTime.modifyDay(day: Int): LocalDateTime
    fun LocalDateTime.modifyHour(hour: Int): LocalDateTime
    fun LocalDateTime.modifyMinute(minute: Int): LocalDateTime
    fun LocalDateTime.modifySecond(second: Int): LocalDateTime
    fun LocalDateTime.modify(
        year: Int? = null,
        month: Month?= null,
        day: Int?= null,
        hour: Int?= null,
        minute: Int?= null,
        second: Int?= null,
    ): LocalDateTime
}

private const val hdFormatPattern = "yyyy-MM-dd HH:mm:ss[ SSS]"
private val hdTimeZone = TimeZone.currentSystemDefault()

//@Deprecated("datetimeFormat", ReplaceWith("datetimeFormat(block)"))
//fun <T, R> datetimeFormat(
//    value: T,
//    block: HDDateTimeScope.(T) -> R,
//): R {
//    return HDDateTimeScopeInstance.block(value)
//}

fun <R> datetimeFormat(
    block: HDDateTimeScope.() -> R,
): R {
    return HDDateTimeScopeInstance.block()
}

private object HDDateTimeScopeInstance : HDDateTimeScope {
    override fun Long.toStr(pattern: String): String {
        return time2Str(pattern)
    }

    override fun String.toTime(pattern: String): Long {
        return str2Time(pattern)
    }

    override fun LocalDateTime.toInstantCustom(timeZone: TimeZone): Instant {
        return this.toInstant(timeZone)
    }

    override fun Instant.toLocalDateTimeCustom(timeZone: TimeZone): LocalDateTime {
        return toLocalDateTime(timeZone)
    }

    override fun LocalDateTime.modifyYear(year: Int): LocalDateTime {
        return LocalDate(year, this.month, this.dayOfMonth).atTime(this.time)
    }

    override fun LocalDateTime.modifyMonth(month: Int): LocalDateTime {
        return LocalDate(this.year, month, this.dayOfMonth).atTime(this.time)
    }

    override fun LocalDateTime.modifyDay(day: Int): LocalDateTime {
        return LocalDate(this.year, this.month, day).atTime(this.time)
    }

    override fun LocalDateTime.modifyHour(hour: Int): LocalDateTime {
        return LocalTime(hour, this.minute, this.second, this.nanosecond).atDate(this.date)
    }

    override fun LocalDateTime.modifyMinute(minute: Int): LocalDateTime {
        return LocalTime(this.hour, minute, this.second, this.nanosecond).atDate(this.date)
    }

    override fun LocalDateTime.modifySecond(second: Int): LocalDateTime {
        return LocalTime(this.hour, this.minute, second, this.nanosecond).atDate(this.date)
    }

    override fun LocalDateTime.modify(
        year: Int?,
        month: Month?,
        day: Int?,
        hour: Int?,
        minute: Int?,
        second: Int?,
    ): LocalDateTime {
        val localTime = LocalTime(
            hour ?: this.hour,
            minute ?: this.minute,
            second ?: this.second,
            this.nanosecond
        )
        val localDate = LocalDate(year ?: this.year, month ?: this.month, day ?: this.dayOfMonth)
        return localTime.atDate(localDate)
    }


}

fun isLeapYear(prolepticYear: Int): Boolean {
    return prolepticYear and 3 == 0 && (prolepticYear % 100 != 0 || prolepticYear % 400 == 0)
}


@OptIn(FormatStringsInDatetimeFormats::class)
private fun Long.time2Str(pattern: String): String {
    val fromEpochMilliseconds = Instant.fromEpochMilliseconds(this)
    val localDateTime = fromEpochMilliseconds.toLocalDateTime(hdTimeZone)
    val format = LocalDateTime.Format {

        byUnicodePattern(pattern)
    }
    return localDateTime.format(format)
}

@OptIn(FormatStringsInDatetimeFormats::class)
private fun String.str2Time(pattern: String): Long {
    val format = LocalDateTime.Format {
        byUnicodePattern(pattern)
    }
    val parse = format.parse(this)
    return parse.toInstant(hdTimeZone).toEpochMilliseconds()
}
