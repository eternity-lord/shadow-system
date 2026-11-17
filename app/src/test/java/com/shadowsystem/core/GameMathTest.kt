package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank
import com.shadowsystem.core.GameMath.Stat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMathTest {

    @Test
    fun xpCurveMatchesSpec() {
        assertEquals(100, GameMath.xpForLevel(1))
        assertEquals(264, GameMath.xpForLevel(2))
        assertEquals(GameMath.xpForLevel(1) + GameMath.xpForLevel(2), GameMath.totalXpForLevel(3).toInt())
    }

    @Test
    fun levelFromTotalXp() {
        assertEquals(1, GameMath.levelForXp(0))
        assertEquals(2, GameMath.levelForXp(100))
        assertEquals(3, GameMath.levelForXp(364))
    }

    @Test
    fun rankBoundaries() {
        assertEquals(Rank.E, GameMath.rankForLevel(10))
        assertEquals(Rank.D, GameMath.rankForLevel(11))
        assertEquals(Rank.C, GameMath.rankForLevel(26))
        assertEquals(Rank.B, GameMath.rankForLevel(46))
        assertEquals(Rank.A, GameMath.rankForLevel(71))
        assertEquals(Rank.S, GameMath.rankForLevel(101))
        assertNull(GameMath.nextRankLevel(101))
        assertEquals(26, GameMath.nextRankLevel(11))
    }

    @Test
    fun statAndSoldierCurves() {
        assertEquals(60, GameMath.statXpForLevel(1))
        assertEquals(50, GameMath.soldierXpForLevel(1))
        assertEquals(1, GameMath.statLevelForXp(59))
        assertEquals(2, GameMath.statLevelForXp(60))
    }

    @Test
    fun soldierStagesAndWisdomTiers() {
        assertEquals(1, GameMath.soldierStage(9))
        assertEquals(2, GameMath.soldierStage(10))
        assertEquals(5, GameMath.soldierStage(80))
        assertEquals(1, GameMath.wisdomTier(0))
        assertEquals(2, GameMath.wisdomTier(34))
        assertEquals(3, GameMath.wisdomTier(67))
    }

    @Test
    fun questRewardsMatchTable() {
        assertEquals(GameMath.Reward(10, 5), GameMath.questReward(Rank.E))
        assertEquals(GameMath.Reward(55, 28), GameMath.questReward(Rank.B))
        assertEquals(GameMath.Reward(120, 60), GameMath.questReward(Rank.S))
    }

    @Test
    fun stepXpSplitIsTwentyPercentPerStep() {
        assertEquals(4, GameMath.stepXp(20))
        assertEquals(16, GameMath.finishXp(20, 4))
    }

    @Test
    fun comebackOnlyAfterTwoMissedDaysAndFirstThreeQuests() {
        assertEquals(1.5, GameMath.comebackMultiplier(2, 0), 0.0)
        assertEquals(1.0, GameMath.comebackMultiplier(1, 0), 0.0)
        assertEquals(1.0, GameMath.comebackMultiplier(3, 3), 0.0)
    }

    @Test
    fun dailyScoreFullDayIsHundred() {
        val score = GameMath.dailyScore(
            GameMath.DailyScoreInput(
                completedXp = 100, plannedXp = 100,
                dungeonMinutes = 25, dungeonGoalMinutes = 25,
                loggedTransactionToday = true, withinBudgetPace = true,
                vitQuestsDone = 1, vitQuestsPlanned = 1, hardDay = false
            )
        )
        assertEquals(100, score)
    }

    @Test
    fun dailyScoreEmptyDayIsZeroOrFinanceOnly() {
        val score = GameMath.dailyScore(
            GameMath.DailyScoreInput(0, 0, 0, 25, false, false, 0, 0, hardDay = false)
        )
        assertEquals(0, score)
    }

    @Test
    fun hardDayScalesTargetsDown() {
        val normal = GameMath.dailyScore(
            GameMath.DailyScoreInput(40, 100, 0, 25, false, false, 0, 0, hardDay = false)
        )
        val hard = GameMath.dailyScore(
            GameMath.DailyScoreInput(40, 100, 0, 25, false, false, 0, 0, hardDay = true)
        )
        assertTrue(hard > normal)
    }

    @Test
    fun shadowBonusOnlyWhenBeatingYesterday() {
        assertEquals(10, GameMath.shadowBonus(70, 60))
        assertEquals(0, GameMath.shadowBonus(60, 60))
    }

    @Test
    fun partialDungeonXpIsProportional() {
        assertEquals(20, GameMath.partialDungeonXp(15, 15, 20))
        assertEquals(9, GameMath.partialDungeonXp(15, 7, 20))
        assertEquals(0, GameMath.partialDungeonXp(0, 5, 20))
    }

    @Test
    fun classFromTopTwoStats() {
        assertEquals("جنگجو", GameMath.className(setOf(Stat.STR, Stat.VIT)))
        assertEquals("حکیم", GameMath.className(setOf(Stat.INT, Stat.VIT)))
    }

    @Test
    fun dungeonDefaultsMatchSpec() {
        assertEquals(15, GameMath.dungeonMinutes(Rank.E))
        assertEquals(60, GameMath.dungeonMinutes(Rank.S))
    }
}
