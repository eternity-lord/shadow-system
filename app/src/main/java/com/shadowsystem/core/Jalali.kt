package com.shadowsystem.core

import java.time.LocalDate

/**
 * Jalali (Shamsi) calendar conversion, Borkowski algorithm (same as jalaali-js).
 * Pure Kotlin/JVM, no Android dependencies, fully unit tested.
 * Supported range: Jalaali years -61..3177.
 */
object Jalali {

    data class Date(val year: Int, val month: Int, val day: Int)

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val WEEKDAY_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    // Truncating division / remainder (matches the reference algorithm).
    private fun div(a: Int, b: Int): Int = a / b
    private fun mod(a: Int, b: Int): Int = a - div(a, b) * b

    private data class JalCal(val gy: Int, val march: Int, val leap: Int)

    private fun jalCal(jy: Int): JalCal {
        require(jy in BREAKS.first()..BREAKS.last() - 1) { "Jalaali year out of range: $jy" }
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0
        for (i in 1 until BREAKS.size) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
        }
        val n = jy - jp
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        var adjusted = n
        if (jump - n < 6) adjusted = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(adjusted + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCal(gy, march, leap)
    }

    fun isLeap(jy: Int): Boolean = jalCal(jy).leap == 0

    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeap(jy)) 30 else 29
    }

    fun yearLength(jy: Int): Int = if (isLeap(jy)) 366 else 365

    fun isValid(jy: Int, jm: Int, jd: Int): Boolean =
        jm in 1..12 && jd in 1..monthLength(jy, jm)

    /** Jalali date -> Gregorian LocalDate. */
    fun toGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        require(isValid(jy, jm, jd)) { "Invalid Jalaali date: $jy/$jm/$jd" }
        val cal = jalCal(jy)
        val base = LocalDate.of(cal.gy, 3, cal.march)
        var offset = jd - 1
        for (m in 1 until jm) offset += monthLength(jy, m)
        return base.plusDays(offset.toLong())
    }

    /** Gregorian LocalDate -> Jalali date. */
    fun fromGregorian(date: LocalDate): Date {
        var jy = date.year - 621
        while (toGregorian(jy, 1, 1).isAfter(date)) jy--
        while (!toGregorian(jy + 1, 1, 1).isAfter(date)) jy++
        var remaining = (date.toEpochDay() - toGregorian(jy, 1, 1).toEpochDay()).toInt()
        var jm = 1
        while (remaining >= monthLength(jy, jm)) {
            remaining -= monthLength(jy, jm)
            jm++
        }
        return Date(jy, jm, remaining + 1)
    }

    fun fromMillis(millis: Long): Date =
        fromGregorian(LocalDate.ofEpochDay(Math.floorDiv(millis, 86_400_000L)))

    fun toMillis(jy: Int, jm: Int, jd: Int): Long =
        toGregorian(jy, jm, jd).toEpochDay() * 86_400_000L

    /** Day of week for a Jalali date: 0 = Saturday (Shanbe) .. 6 = Friday (Jome). */
    fun weekday(jy: Int, jm: Int, jd: Int): Int {
        val gregorian = toGregorian(jy, jm, jd)
        // java.time: Monday=1..Sunday=7. Saturday=6, Sunday=7 -> map to 0,1.
        return when (gregorian.dayOfWeek.value) {
            6 -> 0 // Saturday
            7 -> 1 // Sunday
            else -> gregorian.dayOfWeek.value + 1 // Mon(1)->2 .. Fri(5)->6
        }
    }

    fun weekdayName(jy: Int, jm: Int, jd: Int): String = WEEKDAY_NAMES[weekday(jy, jm, jd)]

    fun monthName(jm: Int): String = MONTH_NAMES[jm - 1]

    /** "۲۵ آبان ۱۴۰۵" style display with Persian digits. */
    fun format(jy: Int, jm: Int, jd: Int): String =
        "${PersianText.toFa(jd)} ${monthName(jm)} ${PersianText.toFa(jy)}"

    fun format(date: Date): String = format(date.year, date.month, date.day)

    fun formatMillis(millis: Long): String = format(fromMillis(millis))

    /** First day (millis) of the Jalali month containing [millis]. */
    fun monthStartMillis(millis: Long): Long {
        val d = fromMillis(millis)
        return toMillis(d.year, d.month, 1)
    }

    /** First day (millis) of the next Jalali month. */
    fun nextMonthStartMillis(millis: Long): Long {
        val d = fromMillis(millis)
        return if (d.month == 12) toMillis(d.year + 1, 1, 1) else toMillis(d.year, d.month + 1, 1)
    }

    /** Add [months] Jalali months to a date (clamps the day). */
    fun addMonths(date: Date, months: Int): Date {
        val total = (date.year * 12 + (date.month - 1)) + months
        val y = Math.floorDiv(total, 12)
        val m = Math.floorMod(total, 12) + 1
        return Date(y, m, minOf(date.day, monthLength(y, m)))
    }
}
