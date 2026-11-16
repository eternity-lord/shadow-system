package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Comeback bonus (spec section 3): after 2 or more missed days, the first 3 quests of the next active day get x1.5. */
class ComebackTest {

    @Test
    fun oneMissedDayIsNotAComeback() {
        // Active on day 10, back on day 11: only 0 full days missed.
        assertFalse(DayRules.isComeback(lastActiveDay = 10, today = 11))
    }

    @Test
    fun twoMissedDaysIsAComeback() {
        // Active on day 10, back on day 13: days 11 and 12 were missed.
        assertTrue(DayRules.isComeback(lastActiveDay = 10, today = 13))
    }

    @Test
    fun neverActiveIsNotAComeback() {
        assertFalse(DayRules.isComeback(lastActiveDay = -1, today = 400))
    }

    @Test
    fun bonusAppliesToFirstThreeQuestsOnly() {
        assertEquals(1.5, GameMath.comebackMultiplier(missedDays = 2, questsCompletedToday = 0), 0.0)
        assertEquals(1.5, GameMath.comebackMultiplier(missedDays = 2, questsCompletedToday = 2), 0.0)
        assertEquals(1.0, GameMath.comebackMultiplier(missedDays = 2, questsCompletedToday = 3), 0.0)
    }

    @Test
    fun noBonusWithoutEnoughMissedDays() {
        assertEquals(1.0, GameMath.comebackMultiplier(missedDays = 1, questsCompletedToday = 0), 0.0)
    }
}
