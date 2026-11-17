package com.shadowsystem.core

import com.shadowsystem.core.SoldierProgress.State
import org.junit.Assert.assertEquals
import org.junit.Test

class SoldierProgressTest {
    private fun q(key: String, day: Long, title: String = "t$day", steps: Int = 1) =
        LinkedQuest(key, day, title, steps)

    @Test
    fun emptyHabitIsLevelOneAndSleepingEgg() {
        val p = SoldierProgress.progress(emptyList(), today = 100L)
        assertEquals(1, p.level)
        assertEquals(1, p.stage)
        assertEquals(0, p.activeDays)
        assertEquals(State.SLEEPING, p.state)
    }

    @Test
    fun xpAndLevelFollowSpecCurve() {
        // 7 quests = 140 XP. Level 1 needs round(50·1^1.3)=50, so 90 XP left; level 2 needs round(50·2^1.3)=123.
        val quests = (0 until 7).map { q("igris", it.toLong()) }
        val p = SoldierProgress.progress(quests, today = 6L)
        assertEquals(140L, p.xp)
        assertEquals(2, p.level)
        assertEquals(90L, p.levelXp)
        assertEquals(123, p.levelNeed)
        assertEquals(7, p.activeDays)
    }

    @Test
    fun activeDaysCountDistinctDaysNotQuests() {
        val quests = listOf(q("igris", 1), q("igris", 1, "other"), q("igris", 2))
        assertEquals(2, SoldierProgress.progress(quests, today = 2L).activeDays)
    }

    @Test
    fun stateFollowsGapSinceLastActiveDay() {
        assertEquals(State.AWAKE, SoldierProgress.stateFor(10L, today = 11L))
        assertEquals(State.TIRED, SoldierProgress.stateFor(10L, today = 12L))
        assertEquals(State.TIRED, SoldierProgress.stateFor(10L, today = 14L))
        assertEquals(State.SLEEPING, SoldierProgress.stateFor(10L, today = 15L))
    }

    @Test
    fun wisdomTiersHaveThresholds() {
        assertEquals(0, SoldierProgress.wisdomTier(9))
        assertEquals(1, SoldierProgress.wisdomTier(10))
        assertEquals(2, SoldierProgress.wisdomTier(40))
        assertEquals(3, SoldierProgress.wisdomTier(100))
    }

    @Test
    fun wisdomRewardsVarietyAndSteps() {
        val quests = listOf(q("tusk", 1, "a", 3), q("tusk", 2, "a", 3), q("tusk", 3, "b", 2))
        // distinct titles 2 × 2 = 4, plus steps 8
        assertEquals(12, SoldierProgress.progress(quests, today = 3L).wisdomScore)
    }
}
