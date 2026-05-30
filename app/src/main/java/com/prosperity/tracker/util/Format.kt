package com.prosperity.tracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/** Formats a whole-Rupiah Long as "Rp 1.250.000" (or "-Rp ..." when negative). */
fun formatRupiah(value: Long): String {
    val sign = if (value < 0) "-" else ""
    val digits = abs(value).toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "${sign}Rp $digits"
}

/** Compact form for chart labels, e.g. 1_250_000 -> "Rp 1,3jt", 45_000 -> "Rp 45rb". */
fun formatRupiahShort(value: Long): String {
    val v = abs(value)
    return when {
        v >= 1_000_000 -> "Rp ${"%.1f".format(v / 1_000_000.0)}jt"
        v >= 1_000 -> "Rp ${v / 1_000}rb"
        else -> "Rp $v"
    }
}

/** Encodes an epoch-millis instant as year*100 + month (e.g. 202605). */
fun monthKeyOf(epochMillis: Long): Int {
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.year * 100 + date.monthValue
}

/** The month key for today. */
fun currentMonthKey(): Int {
    val d = LocalDate.now()
    return d.year * 100 + d.monthValue
}

/** Human label for a month key, e.g. 202605 -> "Mei 2026". */
fun monthLabel(key: Int): String {
    val year = key / 100
    val month = key % 100
    return "${monthNamesId[month - 1]} $year"
}

/** Short month label, e.g. 202605 -> "Mei". */
fun monthLabelShort(key: Int): String = monthNamesShortId[(key % 100) - 1]

/** Returns the [count] most recent month keys ending at [current], oldest first. */
fun lastMonthKeys(current: Int, count: Int): List<Int> {
    val result = ArrayList<Int>(count)
    var year = current / 100
    var month = current % 100
    repeat(count) {
        result.add(year * 100 + month)
        month -= 1
        if (month == 0) {
            month = 12
            year -= 1
        }
    }
    return result.reversed()
}

private val monthNamesId = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

private val monthNamesShortId = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

// ---- Year / day helpers (added for the new Reports screen) ----

/** Year part of an epoch-millis instant. */
fun yearOf(epochMillis: Long): Int =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().year

/** Encodes a day as year*10000 + month*100 + day (e.g. 28 May 2026 -> 20260528). */
fun dayKeyOf(epochMillis: Long): Int {
    val d = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return d.year * 10000 + d.monthValue * 100 + d.dayOfMonth
}

/** Day key built from a LocalDate. */
fun dayKeyOf(date: LocalDate): Int =
    date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

/** Inverse of [dayKeyOf]: convert a day key back to a LocalDate. */
fun dayKeyToLocalDate(dayKey: Int): LocalDate =
    LocalDate.of(dayKey / 10000, (dayKey / 100) % 100, dayKey % 100)

/** The day key for today. */
fun todayDayKey(): Int = dayKeyOf(LocalDate.now())

/** The current year. */
fun currentYear(): Int = LocalDate.now().year

/** Human label for a day key, e.g. 20260528 -> "28 Mei 2026". */
fun dayLabel(dayKey: Int): String {
    val d = dayKeyToLocalDate(dayKey)
    return "${d.dayOfMonth} ${monthNamesId[d.monthValue - 1]} ${d.year}"
}

/** Builds a month key (yyyymm) from numeric year and 1-based month. */
fun monthKeyFromYearMonth(year: Int, month: Int): Int = year * 100 + month

/** Number of days in the given month key. */
fun daysInMonth(monthKey: Int): Int {
    val year = monthKey / 100
    val month = monthKey % 100
    return LocalDate.of(year, month, 1).lengthOfMonth()
}

/**
 * Zero-based offset of the 1st of the given month in a Monday-first calendar grid.
 * Monday -> 0, Tuesday -> 1, ..., Sunday -> 6.
 */
fun firstWeekdayOffsetMondayFirst(monthKey: Int): Int {
    val year = monthKey / 100
    val month = monthKey % 100
    return LocalDate.of(year, month, 1).dayOfWeek.value - 1
}

/** Indonesian day-of-week headers in Monday-first order (single letter). */
val weekdayHeadersIdShort: List<String> = listOf("S", "S", "R", "K", "J", "S", "M")
