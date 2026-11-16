package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyBossTest {
    @Test
    fun hpDropsWithMinutesAndQuestsEachHalf() {
        assertEquals(100, WeeklyBoss.status(0, 0).hpPercent)
        assertEquals(75, WeeklyBoss.status(60, 0).hpPercent)
        assertEquals(50, WeeklyBoss.status(0, 3).hpPercent)
    }

    @Test
    fun defeatedOnlyWhenBothPartsAreDone() {
        assertTrue(WeeklyBoss.status(120, 3).defeated)
        assertTrue(WeeklyBoss.status(500, 9).defeated)
        assertFalse(WeeklyBoss.status(119, 3).defeated)
        assertFalse(WeeklyBoss.status(120, 2).defeated)
    }

    @Test
    fun weekIdUsesSevenDayBlocks() {
        assertEquals(WeeklyBoss.weekId(13), WeeklyBoss.weekId(7))
        assertEquals(2L, WeeklyBoss.weekId(14))
    }

    @Test
    fun heavyRewardAlternatesByWeek() {
        assertEquals("outfit_shadow", WeeklyBoss.heavyRewardFor(0))
        assertEquals("bg_throne", WeeklyBoss.heavyRewardFor(1))
    }
}
