package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecurrenceAndInstallmentTest {
    private fun day(jy: Int, jm: Int, jd: Int): Long = Jalali.toGregorian(jy, jm, jd).toEpochDay()

    @Test
    fun monthlyCatchUpCountsEveryDueMonth() {
        val anchor = day(1404, 1, 1)
        // 1/1, 2/1 and 3/1 are due by 3/1; 4/1 is not.
        assertEquals(3, RecurrenceRules.dueCount(anchor, posted = 0, today = day(1404, 3, 1), period = RecurrencePeriod.MONTHLY))
        // Already posted two: only one more is due.
        assertEquals(1, RecurrenceRules.dueCount(anchor, posted = 2, today = day(1404, 3, 1), period = RecurrencePeriod.MONTHLY))
    }

    @Test
    fun catchUpIsCappedForSafety() {
        val anchor = day(1380, 1, 1)
        assertEquals(RecurrenceRules.MAX_CATCH_UP, RecurrenceRules.dueCount(anchor, 0, day(1404, 1, 1), period = RecurrencePeriod.MONTHLY))
    }

    @Test
    fun monthEndAnchorClampsButDoesNotDrift() {
        val anchor = day(1404, 6, 31)
        // Mehr has 30 days, so the second occurrence is the 30th.
        assertEquals(day(1404, 7, 30), RecurrenceRules.occurrence(anchor, 1, period = RecurrencePeriod.MONTHLY))
        // Esfand 1404 has 29 days.
        assertEquals(day(1404, 12, 29), RecurrenceRules.occurrence(anchor, 6, period = RecurrencePeriod.MONTHLY))
        // Back to 31 in the next year: the anchor day is not lost.
        assertEquals(day(1405, 1, 31), RecurrenceRules.occurrence(anchor, 7, period = RecurrencePeriod.MONTHLY))
    }

    @Test
    fun weeklyRulesStepSevenDays() {
        val anchor = day(1404, 1, 1)
        assertEquals(anchor + 14, RecurrenceRules.occurrence(anchor, 2, period = RecurrencePeriod.WEEKLY))
    }

    @Test
    fun installmentsSumExactlyToTotal() {
        assertEquals(333L, FinanceRules.installmentPart(1000L, 3, 0))
        assertEquals(333L, FinanceRules.installmentPart(1000L, 3, 1))
        assertEquals(334L, FinanceRules.installmentPart(1000L, 3, 2))
        val parts = (0 until 7).map { FinanceRules.installmentPart(10_000_001L, 7, it) }
        assertEquals(10_000_001L, parts.sum())
    }

    @Test
    fun rolloverIsCapped() {
        assertEquals(2000L, FinanceRules.rolloverLimit(1000L, 1000L, 0L))
    }

    @Test
    fun dailyRulePostsEveryDay() {
        val anchor = day(1404, 1, 1)
        assertEquals(anchor + 1, RecurrenceRules.occurrence(anchor, 1, RecurrencePeriod.DAILY))
        assertEquals(2, RecurrenceRules.dueCount(anchor, 0, anchor + 1, RecurrencePeriod.DAILY))
    }

    @Test
    fun yearlyRuleAdvancesTwelveJalaliMonthsPerOccurrence() {
        val anchor = day(1404, 3, 10)
        assertEquals(day(1405, 3, 10), RecurrenceRules.occurrence(anchor, 1, RecurrencePeriod.YEARLY))
        // The anchor itself and the first renewal are both due on 1405/3/10.
        assertEquals(2, RecurrenceRules.dueCount(anchor, 0, day(1405, 3, 10), RecurrencePeriod.YEARLY))
    }

    @Test
    fun unknownStoredPeriodFallsBackToMonthly() {
        assertEquals(RecurrencePeriod.MONTHLY, RecurrencePeriod.parse("NOPE"))
        assertEquals(RecurrencePeriod.YEARLY, RecurrencePeriod.parse("YEARLY"))
    }
}
