package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleRulesTest {
    private val none = TitleRules.Facts(
        level = 1, activeDays = 0, rankIndex = 0, questsDone = 0, dungeonsCompleted = 0, transactions = 0,
        bestScore = 0, beatYesterday = false, goalDone = false, soldiersSummoned = 0, bossDone = false
    )

    @Test
    fun newPlayerHasNothing() {
        assertTrue(TitleRules.titles(none).isEmpty())
        assertTrue(TitleRules.achievements(none).isEmpty())
    }

    @Test
    fun titleThresholds() {
        val f = none.copy(level = 5, activeDays = 10, transactions = 30, dungeonsCompleted = 10, rankIndex = 5)
        assertEquals(setOf("awakened", "unbroken", "shadow_accountant", "dungeon_runner", "monarch"), TitleRules.titles(f))
        assertFalse(TitleRules.titles(none.copy(level = 4)).contains("awakened"))
    }

    @Test
    fun achievementThresholds() {
        val a = TitleRules.achievements(none.copy(questsDone = 1, bestScore = 80, rankIndex = 2))
        assertTrue(a.containsAll(setOf("first_quest", "score_80", "rank_c")))
        assertFalse(a.contains("rank_s"))
        assertTrue(TitleRules.achievements(none.copy(rankIndex = 5)).contains("rank_s"))
    }

    @Test
    fun poseFollowsScore() {
        assertEquals(0, TitleRules.pose(null))
        assertEquals(1, TitleRules.pose(85))
        assertEquals(2, TitleRules.pose(50))
        assertEquals(3, TitleRules.pose(20))
        assertEquals(4, TitleRules.pose(19))
    }
}
