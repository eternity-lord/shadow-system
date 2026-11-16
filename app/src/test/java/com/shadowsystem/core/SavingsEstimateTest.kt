package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
