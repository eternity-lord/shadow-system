package com.shadowsystem.core

import kotlin.math.pow
import kotlin.math.roundToInt

/** All game formulas from the spec (section 3). Pure functions, unit tested. */
object GameMath {

    enum class Rank(val key: String) { E("E"), D("D"), C("C"), B("B"), A("A"), S("S") }

    enum class Stat { STR, INT, AGI, VIT, SENSE }

    /** XP needed to go from level n to n+1: round(100 * n^1.4) */
    fun xpForLevel(n: Int): Int = (100 * n.toDouble().pow(1.4)).roundToInt()

    /** Total XP accumulated to reach [level] from level 1. */
    fun totalXpForLevel(level: Int): Long {
        var total = 0L
        for (n in 1 until level) total += xpForLevel(n)
        return total
    }

    /** Level reached with [totalXp] (starts at 1). */
    fun levelForXp(totalXp: Long): Int {
        var level = 1
        var remaining = totalXp
        while (remaining >= xpForLevel(level)) {
            remaining -= xpForLevel(level)
            level++
        }
        return level
    }

    fun rankForLevel(level: Int): Rank = when {
        level <= 10 -> Rank.E
        level <= 25 -> Rank.D
        level <= 45 -> Rank.C
        level <= 70 -> Rank.B
        level <= 100 -> Rank.A
        else -> Rank.S
    }

    /** First level of the next rank, or null at S. */
    fun nextRankLevel(level: Int): Int? = when (rankForLevel(level)) {
        Rank.E -> 11
        Rank.D -> 26
        Rank.C -> 46
        Rank.B -> 71
        Rank.A -> 101
        Rank.S -> null
    }

    /** Stat XP per step: round(60 * n^1.35) */
    fun statXpForLevel(n: Int): Int = (60 * n.toDouble().pow(1.35)).roundToInt()

    fun statLevelForXp(totalXp: Long): Int {
        var level = 1
        var remaining = totalXp
        while (remaining >= statXpForLevel(level)) {
            remaining -= statXpForLevel(level)
            level++
        }
        return level
    }

    /** Soldier XP per step: round(50 * n^1.3) */
    fun soldierXpForLevel(n: Int): Int = (50 * n.toDouble().pow(1.3)).roundToInt()

    fun soldierLevelForXp(totalXp: Long): Int {
        var level = 1
        var remaining = totalXp
        while (remaining >= soldierXpForLevel(level)) {
            remaining -= soldierXpForLevel(level)
            level++
        }
        return level
    }

    /** Appearance stage 1..5 by soldier level. */
    fun soldierStage(level: Int): Int = when {
        level <= 9 -> 1
        level <= 24 -> 2
        level <= 49 -> 3
        level <= 79 -> 4
        else -> 5
    }

    /** Wisdom overlay tier 1..3 from a 0..100 wisdom score. */
    fun wisdomTier(wisdom: Int): Int = when {
        wisdom < 34 -> 1
        wisdom < 67 -> 2
        else -> 3
    }

    data class Reward(val xp: Int, val gold: Int)

    fun questReward(rank: Rank): Reward = when (rank) {
        Rank.E -> Reward(10, 5)
        Rank.D -> Reward(20, 10)
        Rank.C -> Reward(35, 18)
        Rank.B -> Reward(55, 28)
        Rank.A -> Reward(80, 40)
        Rank.S -> Reward(120, 60)
    }

    /** Default dungeon minutes per rank. */
    fun dungeonMinutes(rank: Rank): Int = when (rank) {
        Rank.E -> 15
        Rank.D -> 20
        Rank.C -> 25
        Rank.B -> 35
        Rank.A -> 45
        Rank.S -> 60
    }

    /** XP awarded for one completed step: 20% of quest XP. */
    fun stepXp(questXp: Int): Int = (questXp * 0.2).roundToInt()

    /** XP awarded on finishing a quest: the remaining 80%. */
    fun finishXp(questXp: Int, alreadyAwarded: Int): Int = (questXp - alreadyAwarded).coerceAtLeast(0)

    /** Comeback bonus multiplier for the first 3 quests after >=2 missed days. */
    fun comebackMultiplier(missedDays: Int, questsCompletedToday: Int): Double =
        if (missedDays >= 2 && questsCompletedToday < 3) 1.5 else 1.0

    fun applyMultiplier(xp: Int, multiplier: Double): Int = (xp * multiplier).roundToInt()

    data class DailyScoreInput(
        val completedXp: Int,
        val plannedXp: Int,
        val dungeonMinutes: Int,
        val dungeonGoalMinutes: Int,
        val loggedTransactionToday: Boolean,
        val withinBudgetPace: Boolean,
        val vitQuestsDone: Int,
        val vitQuestsPlanned: Int,
        val hardDay: Boolean
    )

    /**
     * Daily score 0..100: 40% quests, 20% dungeon, 20% finance, 20% VIT.
     * Hard day lowers every target proportionally (same score reachable).
     */
    fun dailyScore(input: DailyScoreInput): Int {
        val targetScale = if (input.hardDay) 0.4 else 1.0
        val questPart = if (input.plannedXp <= 0) 0.0
        else (input.completedXp.toDouble() / (input.plannedXp * targetScale)).coerceIn(0.0, 1.0) * 40
        val dungeonTarget = (input.dungeonGoalMinutes * targetScale).coerceAtLeast(1.0)
        val dungeonPart = (input.dungeonMinutes / dungeonTarget).coerceIn(0.0, 1.0) * 20
        val financePart = (if (input.loggedTransactionToday) 10 else 0) +
            (if (input.withinBudgetPace) 10 else 0)
        val vitTarget = (input.vitQuestsPlanned * targetScale).coerceAtLeast(1.0)
        val vitPart = (input.vitQuestsDone / vitTarget).coerceIn(0.0, 1.0) * 20
        return (questPart + dungeonPart + financePart + vitPart).roundToInt().coerceIn(0, 100)
    }

    /** Gold bonus for beating yesterday's score. */
    fun shadowBonus(today: Int, yesterday: Int): Int = if (today > yesterday) 10 else 0

    /** Partial XP for a cancelled dungeon session, proportional to minutes. */
    fun partialDungeonXp(plannedMinutes: Int, doneMinutes: Int, fullXp: Int): Int {
        if (plannedMinutes <= 0) return 0
        return (fullXp * doneMinutes.toDouble() / plannedMinutes).roundToInt().coerceIn(0, fullXp)
    }

    /** Named class from the top two stats. */
    fun className(topTwo: Set<Stat>): String = when (topTwo) {
        setOf(Stat.STR, Stat.VIT) -> "جنگجو"
        setOf(Stat.INT, Stat.SENSE) -> "دانشمند"
        setOf(Stat.AGI, Stat.SENSE) -> "آدمکش"
        setOf(Stat.INT, Stat.VIT) -> "حکیم"
        setOf(Stat.STR, Stat.AGI) -> "شکارچی"
        setOf(Stat.STR, Stat.INT) -> "فرمانده"
        setOf(Stat.STR, Stat.SENSE) -> "نگهبان"
        setOf(Stat.INT, Stat.AGI) -> "مخترع"
        setOf(Stat.AGI, Stat.VIT) -> "دونده"
        setOf(Stat.VIT, Stat.SENSE) -> "مراقب"
        else -> "شکارچی"
    }
}
