package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RolloverTest {
    @Test
    fun unspentCarriesOver() {
        assertEquals(1500L, FinanceRules.rolloverLimit(baseLimit = 1000L, prevLimit = 1000L, prevSpent = 500L))
    }

    @Test
    fun carryIsCappedAtOneBaseLimit() {
        assertEquals(2000L, FinanceRules.rolloverLimit(baseLimit = 1000L, prevLimit = 5000L, prevSpent = 0L))
    }

    @Test
    fun overspendDoesNotReduceNextMonth() {
        assertEquals(1000L, FinanceRules.rolloverLimit(baseLimit = 1000L, prevLimit = 1000L, prevSpent = 1800L))
    }

    @Test
    fun zeroBaseGivesNoLimit() {
        assertEquals(0L, FinanceRules.rolloverLimit(baseLimit = 0L, prevLimit = 500L, prevSpent = 0L))
    }
}
