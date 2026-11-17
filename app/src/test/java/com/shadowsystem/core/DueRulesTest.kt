package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DueRulesTest {

    private val day = 20_000L

    @Test
    fun firstInstallmentIsTheFirstDueDay() {
        assertEquals(day, DueRules.installmentDay(day, 0))
    }

    @Test
    fun laterInstallmentsFallAboutAMonthApart() {
        val second = DueRules.installmentDay(day, 1)
        assertTrue(second - day in 28L..31L)
        val third = DueRules.installmentDay(day, 2)
        assertTrue(third - second in 28L..31L)
    }

    @Test
    fun debtDueDayFollowsTheNextUnpaidInstallment() {
        assertNull(DueRules.debtDueDay(null, 3, 0))
        assertEquals(day, DueRules.debtDueDay(day, 0, 0))
        assertEquals(DueRules.installmentDay(day, 1), DueRules.debtDueDay(day, 3, 1))
    }

    @Test
    fun attentionCoversSoonAndOverdueButNotPaidOrLater() {
        assertTrue(DueRules.needsAttention(dueDay = 103, today = 100, done = false))
        assertTrue(DueRules.needsAttention(dueDay = 99, today = 100, done = false))
        assertFalse(DueRules.needsAttention(dueDay = 104, today = 100, done = false))
        assertFalse(DueRules.needsAttention(dueDay = 99, today = 100, done = true))
    }
}
