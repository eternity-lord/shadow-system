package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsEstimateTest {
    @Test
    fun estimateRoundsUpToWholeDays() {
        assertEquals(10L, FinanceRules.savingsEstimateDays(remaining = 1000L, savedPerDay = 100.0))
        assertEquals(4L, FinanceRules.savingsEstimateDays(remaining = 1000L, savedPerDay = 300.0))
    }

    @Test
    fun noSavingMeansNoEstimate() {
        assertNull(FinanceRules.savingsEstimateDays(remaining = 1000L, savedPerDay = 0.0))
    }

    @Test
    fun metGoalIsZeroDays() {
        assertEquals(0L, FinanceRules.savingsEstimateDays(remaining = 0L, savedPerDay = 0.0))
    }

    @Test
    fun goalCompletesOnlyOnTheCrossingDeposit() {
        assertTrue(FinanceRules.goalJustCompleted(1_000L, 900L, 1_000L))
        assertTrue(FinanceRules.goalJustCompleted(1_000L, 900L, 1_500L))
        assertFalse(FinanceRules.goalJustCompleted(1_000L, 1_000L, 1_200L))
        assertFalse(FinanceRules.goalJustCompleted(1_000L, 500L, 800L))
        assertFalse(FinanceRules.goalJustCompleted(0L, 0L, 100L))
    }

    @Test
    fun changePercentAndDailyAverage() {
        assertEquals(50, FinanceRules.changePercent(150L, 100L))
        assertEquals(-20, FinanceRules.changePercent(80L, 100L))
        assertNull(FinanceRules.changePercent(80L, 0L))
        assertEquals(100L, FinanceRules.dailyAverage(1_000L, 10L))
        assertEquals(0L, FinanceRules.dailyAverage(1_000L, 0L))
    }
}
