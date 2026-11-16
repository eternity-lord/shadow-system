package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DayRulesTest {

    private val berlin = ZoneId.of("Europe/Berlin")

    private fun millis(y: Int, mo: Int, d: Int, h: Int, mi: Int) =
        ZonedDateTime.of(y, mo, d, h, mi, 0, 0, berlin).toInstant().toEpochMilli()

    @Test
    fun dayRollsOverAtFourAm() {
        val before = DayRules.gameDay(millis(2026, 11, 16, 3, 59), berlin)
        val after = DayRules.gameDay(millis(2026, 11, 16, 4, 0), berlin)
        assertEquals(before + 1, after)
        assertEquals(DayRules.dateOf(before).plusDays(1), DayRules.dateOf(after))
    }

    @Test
    fun comebackNeedsTwoFullMissedDays() {
        assertFalse(DayRules.isComeback(10, 11))
        assertFalse(DayRules.isComeback(10, 12))
        assertTrue(DayRules.isComeback(10, 13))
        assertFalse(DayRules.isComeback(-1, 13))
    }
}
