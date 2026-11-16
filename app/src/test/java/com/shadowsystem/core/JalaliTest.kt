package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JalaliTest {

    @Test
    fun newYearDatesMatchKnownGregorianDays() {
        assertEquals(LocalDate.of(2024, 3, 20), Jalali.toGregorian(1403, 1, 1))
        assertEquals(LocalDate.of(2025, 3, 21), Jalali.toGregorian(1404, 1, 1))
        assertEquals(LocalDate.of(2026, 3, 21), Jalali.toGregorian(1405, 1, 1))
    }

    @Test
    fun todayIsAban25In1405() {
        assertEquals(Jalali.Date(1405, 8, 25), Jalali.fromGregorian(LocalDate.of(2026, 11, 16)))
    }

    @Test
    fun leapYearsAreDetected() {
        assertTrue(Jalali.isLeap(1399))
        assertTrue(Jalali.isLeap(1403))
        assertTrue(Jalali.isLeap(1408))
        assertFalse(Jalali.isLeap(1404))
        assertFalse(Jalali.isLeap(1405))
    }

    @Test
    fun monthLengthsFollowPersianRules() {
        assertEquals(31, Jalali.monthLength(1404, 1))
        assertEquals(31, Jalali.monthLength(1404, 6))
        assertEquals(30, Jalali.monthLength(1404, 7))
        assertEquals(30, Jalali.monthLength(1404, 11))
        assertEquals(29, Jalali.monthLength(1404, 12))
        assertEquals(30, Jalali.monthLength(1403, 12))
    }

    @Test
    fun esfandLastDayRollsToFarvardin() {
        assertEquals(LocalDate.of(2025, 3, 20), Jalali.toGregorian(1403, 12, 30))
        assertEquals(LocalDate.of(2025, 3, 21), Jalali.toGregorian(1404, 1, 1))
    }

    @Test
    fun roundTripForDecade() {
        var d = LocalDate.of(2020, 1, 1)
        val end = LocalDate.of(2031, 1, 1)
        while (d.isBefore(end)) {
            val j = Jalali.fromGregorian(d)
            assertEquals(d, Jalali.toGregorian(j.year, j.month, j.day))
            d = d.plusDays(1)
        }
    }

    @Test
    fun weekdayMapsSaturdayToZeroAndFridayToSix() {
        // 2026-11-16 is a Monday -> index 2 (Doshanbe)
        assertEquals(2, Jalali.weekday(1405, 8, 25))
        // 2026-11-14 is a Saturday -> index 0
        assertEquals(0, Jalali.weekday(1405, 8, 23))
        // 2026-11-20 is a Friday -> index 6
        assertEquals(6, Jalali.weekday(1405, 8, 29))
    }

    @Test
    fun addMonthsClampsDay() {
        val r = Jalali.addMonths(Jalali.Date(1404, 6, 31), 1)
        assertEquals(Jalali.Date(1404, 7, 30), r)
    }
}
