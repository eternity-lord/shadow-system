package com.shadowsystem.core

import com.shadowsystem.core.DailyReport.Trend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyReportTest {
    @Test
    fun trendComparesWithYesterday() {
        assertEquals(Trend.UP, DailyReport.trend(70, 50))
        assertEquals(Trend.SAME, DailyReport.trend(50, 50))
        assertEquals(Trend.DOWN, DailyReport.trend(30, 50))
        assertEquals(Trend.NONE, DailyReport.trend(30, null))
        assertNull(DailyReport.delta(30, null))
        assertEquals(-20, DailyReport.delta(30, 50))
    }

    @Test
    fun nightWindowStartsAtTwentyAndEndsAtBoundary() {
        assertTrue(DailyReport.isNightHour(20))
        assertTrue(DailyReport.isNightHour(3))
        assertFalse(DailyReport.isNightHour(4))
        assertFalse(DailyReport.isNightHour(12))
    }
}
