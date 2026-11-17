package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursTest {

    @Test
    fun windowCrossingMidnight() {
        assertTrue(QuietHours.isQuiet(23, start = 22, end = 7))
        assertTrue(QuietHours.isQuiet(0, start = 22, end = 7))
        assertTrue(QuietHours.isQuiet(6, start = 22, end = 7))
        assertFalse(QuietHours.isQuiet(7, start = 22, end = 7))
        assertFalse(QuietHours.isQuiet(12, start = 22, end = 7))
    }

    @Test
    fun windowInsideOneDay() {
        assertTrue(QuietHours.isQuiet(13, start = 12, end = 14))
        assertFalse(QuietHours.isQuiet(14, start = 12, end = 14))
        assertFalse(QuietHours.isQuiet(11, start = 12, end = 14))
    }

    @Test
    fun equalBoundsMeanNoQuietTime() {
        assertFalse(QuietHours.isQuiet(5, start = 6, end = 6))
    }

    @Test
    fun shiftWrapsAroundTheDay() {
        assertEquals(0, QuietHours.shift(23, 1))
        assertEquals(23, QuietHours.shift(0, -1))
        assertEquals(10, QuietHours.shift(9, 1))
    }
}
