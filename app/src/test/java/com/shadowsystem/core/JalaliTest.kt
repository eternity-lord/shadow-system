package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JalaliTest {
    @Test
    fun nowruzDatesMatchGregorianCalendar() {
        assertEquals(LocalDate.of(2024, 3, 20), Jalali.toGregorian(1403, 1, 1))
        assertEquals(LocalDate.of(2025, 3, 21), Jalali.toGregorian(1404, 1, 1))
    }

    @Test
    fun leapYearsFollowThirtyThreeYearCycle() {
        assertTrue(Jalali.isLeap(1399))
        assertTrue(Jalali.isLeap(1403))
        assertFalse(Jalali.isLeap(1404))
        assertEquals(30, Jalali.monthLength(1403, 12))
        assertEquals(29, Jalali.monthLength(1404, 12))
        assertFalse(Jalali.isValid(1404, 12, 30))
        assertTrue(Jalali.isValid(1403, 12, 30))
    }

    @Test
    fun monthLengthsAtBoundaries() {
        assertEquals(31, Jalali.monthLength(1404, 1))
        assertEquals(31, Jalali.monthLength(1404, 6))
        assertEquals(30, Jalali.monthLength(1404, 7))
        assertEquals(30, Jalali.monthLength(1404, 11))
    }

    @Test
    fun addMonthsClampsDayToShorterMonth() {
        assertEquals(Jalali.Date(1403, 7, 30), Jalali.addMonths(Jalali.Date(1403, 6, 31), 1))
        assertEquals(Jalali.Date(1404, 1, 1), Jalali.addMonths(Jalali.Date(1403, 12, 1), 1))
        assertEquals(Jalali.Date(1403, 12, 29), Jalali.addMonths(Jalali.Date(1404, 1, 29), -1))
    }

    @Test
    fun roundTripAcrossThreeYears() {
        var d = LocalDate.of(2024, 3, 1)
        repeat(3 * 366) {
            val j = Jalali.fromGregorian(d)
            assertEquals(d, Jalali.toGregorian(j.year, j.month, j.day))
            d = d.plusDays(1)
        }
    }
}
